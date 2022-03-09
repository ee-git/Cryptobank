package nl.miwteam2.cryptomero.service;

import nl.miwteam2.cryptomero.domain.BankAccount;
import nl.miwteam2.cryptomero.domain.Customer;
import nl.miwteam2.cryptomero.domain.UserAccount;
import nl.miwteam2.cryptomero.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.*;

/**
 * @author Samuël Geurts & Stijn Klijn
 */

@Service
public class CustomerService implements GenericService<Customer> {

    private static final String VALID = "Valid";
    private static final double INITIAL_BALANCE = 1000000;

    private CustomerRepository customerRepository;
    private AddressService addressService;
    private BankAccountService bankAccountService;
    private UserAccountService userAccountService;

    @Autowired
    public CustomerService(CustomerRepository customerRepository,AddressService addressService,
            BankAccountService bankAccountService,UserAccountService userAccountService) {
        this.customerRepository = customerRepository;
        this.addressService = addressService;
        this.bankAccountService = bankAccountService;
        this.userAccountService = userAccountService;
    }

    /**
     * Stores a new customer in the database
     * @param customer      The customer to be stored
     * @return              The stored customer if storage was successful
     * @throws Exception    If the customer cannot be stored
     */
    @Override
    public Customer storeOne(Customer customer) throws Exception {

        //Check whether all fields are valid, otherwise throw exception
        String validityString = checkFieldValidity(customer);
        if (!validityString.equals(VALID)) throw new Exception(validityString);

        //Attempt to store customer address and throw exception if address is invalid
        int addressId = addressService.storeAddress(customer.getAddress());
        customer.getAddress().setIdAddress(addressId);
        if (addressId == 0) throw new Exception("Invalid address");

        //Store customer in the database and receive the auto-generated key
        UserAccount userAccount = userAccountService.storeOne(customer);
        customer.setIdAccount(userAccount.getIdAccount());
        customerRepository.storeOne(customer);

        //Generate and store new bank account
        BankAccount bankAccount = new BankAccount(customer, bankAccountService.generateIban(), INITIAL_BALANCE);
        customer.setBankAccount(bankAccount);
        bankAccountService.storeOne(bankAccount);

        //Setup empty wallet
        Map<String, Double> wallet = new HashMap<>();
        customer.setWallet(wallet);

        return customer;
    }

    /**
     * Retrieve customer with the given id from the database
     * @param id            Id of the customer to retrieve
     * @return              The retrieved customer
     */
    @Override
    public Customer findById(int id) {
        return customerRepository.findById(id);
    }

    @Override
    public List<Customer> getAll() {
        //Omitted until required
        return null;
    }

    @Override
    public Customer updateOne(Customer customer) {
        //Omitted until required
        return null;
    }

    @Override
    public Customer deleteOne(int id) {
        //Omitted until required
        return null;
    }

    /**
     * Check whether all fields are valid
     * @param customer      The customer to be stored
     * @return              String representing whether all fields are valid or which error occurred.
     */
    private String checkFieldValidity(Customer customer) throws NoSuchAlgorithmException, IOException, InterruptedException {
        int numberOfBreaches = numberOfPasswordBreaches(customer.getPassword());

        if (!isEveryFieldOfValidLength(customer)) return "Invalid field length";
        if (!isValidEmail(customer.getEmail())) return "Invalid e-mail";
        if (userAccountService.isEmailAlreadyInUse(customer.getEmail())) return "E-mail already in use";
        if (!isValidPassword(customer.getPassword())) return "Invalid password";
        if (numberOfBreaches!=0) return "This password has been seen " + numberOfBreaches + " times before";
        if (!isValidDob(customer.getDob())) return "Invalid dob";
        if (!isValidBsn(customer.getBsn())) return "Invalid bsn";
        return VALID;
    }

    /**
     * Check whether all required fields are not null and are not empty strings, and no fields are too long
     * @param customer      The customer to be stored
     * @return              Boolean representing whether this condition is met
     */
    private boolean isEveryFieldOfValidLength(Customer customer) {
        final int MAX_LENGTH_EMAIL = 30;
        final int MAX_LENGTH_PASSWORD = 64;
        final int MAX_LENGTH_FIRST_NAME = 45;
        final int MAX_LENGTH_NAME_PREFIX = 15;
        final int MAX_LENGTH_LAST_NAME = 45;
        final int MAX_LENGTH_TELEPHONE = 30;

        return customer.getEmail().length() > 0 && customer.getEmail().length() <= MAX_LENGTH_EMAIL &&
                customer.getPassword().length() > 0 && customer.getPassword().length() <= MAX_LENGTH_PASSWORD &&
                customer.getFirstName().length() > 0 &&  customer.getFirstName().length() <= MAX_LENGTH_FIRST_NAME &&
                (customer.getNamePrefix() == null || customer.getNamePrefix().length() < MAX_LENGTH_NAME_PREFIX) &&
                customer.getLastName().length() > 0 && customer.getLastName().length() <= MAX_LENGTH_LAST_NAME &&
                customer.getDob() != null &&
                customer.getTelephone().length() > 0 && customer.getTelephone().length() <= MAX_LENGTH_TELEPHONE &&
                customer.getAddress() != null;
    }

    /**
     * OWASP email validation according to Baeldung
     * @param email         The email to be validated
     * @return              Boolean representing whether this email is valid
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    }

    /**
     * Check whether password is part of haveibeenpwned.com password datatset
     * @param password      The password to be checked
     * @return              Boolean representing whether this password is breached
     */
    private int numberOfPasswordBreaches(String password) throws NoSuchAlgorithmException, IOException, InterruptedException {
        String sha1 = stringToSHa1Hex(password);
        Map<String,Integer> hashedPasswordlist = getHashedPasswordlist(sha1);
        if (hashedPasswordlist.containsKey(sha1)){
            return hashedPasswordlist.get(sha1);
        } else {
            return 0;
        }
    }
    /**
     * turns a String into a Sha-1 hex string
     * @param string       The string to be hashed (a password in this case)
     * @return             The hashed string in hex format
     */
    private String stringToSHa1Hex(String string) throws NoSuchAlgorithmException {
        MessageDigest msdDigest = MessageDigest.getInstance("SHA-1");
        msdDigest.update(string.getBytes());
        byte[] digest = msdDigest.digest();
        return HexFormat.of().formatHex(digest).toUpperCase();
    }

    /**
     * Does a get request at pwnedpasswords.com. The first 5 characters of he hashed password are given within the
     * request. All known hashcodes with the same first 5 characters are returned in the respons body included the
     * number of breaches.
     * @param sha1       The sha-1 string to be checked
     * @return           A map containing sha-1 Strings as keys and the occurrence number as values;
     */
    private Map<String, Integer> getHashedPasswordlist(String sha1) throws IOException, InterruptedException {
        //performs https request
        String url = String.format("https://api.pwnedpasswords.com/range/%s", sha1.substring(0, 5));
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        //transforms respons-body to map
        Map<String,Integer> numberOfBreachesPerPassword = new TreeMap<>();
        String[] arrayOfStr = response.body().split("\n");
        for (String string: arrayOfStr) {
            String sha1part = string.substring(0,string.indexOf(":"));
            Integer numberOfBreaches = Integer.parseInt(string.substring(string.indexOf(":")+1).replace("\r",""));
            numberOfBreachesPerPassword.put(sha1.substring(0, 5) + sha1part,numberOfBreaches);
        }
        return numberOfBreachesPerPassword;
    }

    /**
     * Check whether password conforms to requirements
     * @param password      The password to be checked
     * @return              Boolean representing whether this password meets the requirements
     */
    private boolean isValidPassword(String password) {
        //TODO eventuele eisen aan wachtwoord hier formuleren
        return true;
    }

    /**
     * Check whether date of birth conforms to requirements
     * @param date          The date of birth to be checked
     * @return              Boolean representing whether this dob meets the requirements
     */
    private boolean isValidDob(LocalDate date) {
        final int MIN_AGE = 18;
        return !LocalDate.now().minusYears(MIN_AGE).isBefore(date);
    }

    /**
     * Check whether bsn conforms to "11-proef"
     * @param bsn           The bsn to be checked
     * @return              Boolean representing whether this condition is met
     */
    private boolean isValidBsn(String bsn) {
        //TODO: Als dit via een externe API kan heeft dat de voorkeur, maar die kan ik vooralsnog niet vinden
        final int MIN_LENGTH = 8;
        final int MAX_LENGTH = 9;
        final int[] FACTORS = {9, 8, 7, 6, 5, 4, 3, 2, -1};
        final int DIVISOR = 11;

        if (bsn.length() < MIN_LENGTH || bsn.length() > MAX_LENGTH) return false; //bsn too short or too long
        if (!bsn.matches("\\d+")) return false; //bsn not consisting of only numbers

        if (bsn.length() == 8) bsn = "0" + bsn; //prepend 0 to ensure bsn consists of 9 numbers

        //Apply "11-proef"
        int sum = 0;
        for (int i = 0; i < bsn.length(); i++) {
            int digit = Integer.parseInt(bsn.substring(i, i + 1));
            sum += digit * FACTORS[i];
        }
        return sum % DIVISOR == 0;
    }
}
