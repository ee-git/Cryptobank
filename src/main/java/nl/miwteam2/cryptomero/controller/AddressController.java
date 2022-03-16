package nl.miwteam2.cryptomero.controller;

import com.google.gson.Gson;
import nl.miwteam2.cryptomero.domain.Address;
import nl.miwteam2.cryptomero.service.AddressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * @author Petra Coenen
 * @version 1.5
 */

@RestController
@RequestMapping("/address")
public class AddressController {

    private final AddressService addressService;
    private final Gson gson = new Gson();

    private final Logger logger = LoggerFactory.getLogger(AddressController.class);

    @Autowired
    public AddressController(AddressService service) {
        addressService = service;
        logger.info("New AddressController");
    }

    @PostMapping
    public ResponseEntity<String> storeAddress(@RequestBody Address address) {
        int storeStatus = addressService.storeAddress(address);
        if (storeStatus == 0) {
            return new ResponseEntity<>(gson.toJson("Geen bestaand adres in Nederland"), HttpStatus.BAD_REQUEST);
        } else if (storeStatus == -1) {
            List<String> missingDataList = addressService.checkRequiredFields(address);
            return new ResponseEntity<>(gson.toJson("De volgende velden missen: " + String.join(", ", missingDataList)), HttpStatus.BAD_REQUEST);
        } else return new ResponseEntity<>(gson.toJson("Adres opgeslagen"), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Address> getAddressById(@PathVariable int id) {
        Address addressById = addressService.getAddressById(id);
        if (addressById != null) {
            return new ResponseEntity<>(addressById, HttpStatus.OK);
        } else throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @GetMapping
    public List<Address> getAllAddresses() { return addressService.getAllAddresses(); }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateAddress(@PathVariable int id, @RequestBody Address address) {
        // TODO: PathVariable juist verwerken (PutMapping kan niet zonder)
        int updateStatus = addressService.updateAddress(address);
        if (updateStatus == 1) {
            return new ResponseEntity<>(gson.toJson("Adres is bijgewerkt"), HttpStatus.OK);
        } return new ResponseEntity<>(gson.toJson("Er bestaat geen adres met dit id"), HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAddress(@PathVariable int id) {
        int deleteStatus = addressService.deleteAddress(id);
        if (deleteStatus == 1) {
            return new ResponseEntity<>(gson.toJson("Adres is verwijderd"), HttpStatus.OK);
        } else return new ResponseEntity<>(gson.toJson("Er bestaat geen adres met dit id"), HttpStatus.NOT_FOUND);
    }
}
