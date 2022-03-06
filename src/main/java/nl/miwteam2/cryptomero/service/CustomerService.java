package nl.miwteam2.cryptomero.service;

import nl.miwteam2.cryptomero.domain.BankAccount;
import nl.miwteam2.cryptomero.domain.Customer;
import nl.miwteam2.cryptomero.repository.GenericDao;
import nl.miwteam2.cryptomero.repository.RootRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomerService implements GenericService<Customer> {

    private GenericDao<Customer> customerDao;
    private RootRepository rootRepository;
    private AddressService addressService;
    private UserAccountService userAccountService;

    @Autowired
    public CustomerService(GenericDao<Customer> dao, RootRepository rootRepository,AddressService addressService,UserAccountService userAccountService) {
        this.rootRepository = rootRepository;
        this.customerDao = dao;
        this.addressService = addressService;
        this.userAccountService = userAccountService;
    }

    @Override
    public int storeOne(Customer customer) {
        //Omitted because the store method has to return a Customer
        return 0;
    }

    public Customer storeCustomer(Customer customer) throws Exception {

        if (!isEveryFieldCompleted(customer)) throw new Exception("Not all fields completed");
        if (!isValidEmail(customer.getEmail())) throw new Exception("Invalid e-mail");
        if (!isValidPassword(customer.getPassword())) throw new Exception("Invalid password");
        if (!isValidBsn(customer.getBsn())) throw new Exception("Invalid bsn");

        int addressId = addressService.storeAddress(customer.getAddress());
        customer.getAddress().setIdAddress(addressId);

        if (addressId == 0) throw new Exception("Invalid address");

        int userId = userAccountService.storeOne(customer);
        customer.setIdAccount(userId);
        customerDao.storeOne(customer);

        //TODO: Bank account genereren? En ook teruggeven aan de frontend
        BankAccount bankAccount = new BankAccount("NL00CRME0123456789", 1000000.00);
        customer.setBankAccount(bankAccount);

        Map<String, Double> wallet = new HashMap<>();
        customer.setWallet(wallet);

        return customer;
    }

    @Override
    public Customer findById(int id) {
        return rootRepository.findCustomerById(id);
    }

    @Override
    public List<Customer> getAll() {
        //Omitted until required
        return null;
    }

    @Override
    public int updateOne(Customer customer) {
        //Omitted until required
        return 0;
    }

    @Override
    public int deleteOne(int id) {
        //Omitted until required
        return 0;
    }

    private boolean isEveryFieldCompleted(Customer customer) {
        return customer.getEmail().length() > 0 && customer.getPassword().length() > 0 &&
                customer.getFirstName().length() > 0 && customer.getLastName().length() > 0 &&
                customer.getDob() != null && customer.getBsn().length() > 0 &&
                customer.getTelephone().length() > 0 && customer.getAddress() != null;
    }

    private boolean isValidEmail(String email) {
        //OWASP email validation according to Baeldung
        return email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    }

    private boolean isValidPassword(String password) {
        //TODO eisen aan wachtwoord formuleren
        return true;
    }

    private boolean isValidBsn(String bsn) {
        //TODO: Als dit via een externe API kan heeft dat de voorkeur, maar die kan ik vooralsnog niet vinden
        final int MIN_LENGTH = 8;
        final int MAX_LENGTH = 9;
        final int[] FACTORS = {9, 8, 7, 6, 5, 4, 3, 2, -1};
        if (bsn.length() < MIN_LENGTH || bsn.length() > MAX_LENGTH) {
            //bsn too short or too long
            return false;
        }
        if (!bsn.matches("\\d+")) {
            //bsn not consisting of only numbers
            return false;
        }
        if (bsn.length() == 8) {
            //prepend 0 to ensure bsn consists of 9 numbers
            bsn = "0" + bsn;
        }
        //Apply "11-proef"
        int sum = 0;
        for (int i = 0; i < bsn.length(); i++) {
            int digit = Integer.parseInt(bsn.substring(i, i + 1));
            sum += digit * FACTORS[i];
        }
        return sum % 11 == 0;
    }
}
