package nl.miwteam2.cryptomero.controller;

import nl.miwteam2.cryptomero.domain.Customer;
import nl.miwteam2.cryptomero.domain.TradeOfferDto;
import nl.miwteam2.cryptomero.domain.TradeBankDto;
import nl.miwteam2.cryptomero.service.Authentication.AuthenticationService;
import nl.miwteam2.cryptomero.service.OfferService;
import nl.miwteam2.cryptomero.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints for trade with the bank or with the marketplace
 * @author: Samuel Geurts, studentnr: 500893275 - MIW Cohort 26
 * version 1.0
*/

@CrossOrigin
@RestController
@RequestMapping("/trade")
public class TradeController {

    private TransactionService transactionService;
    private OfferService offerService;
    private AuthenticationService authenticationService;

    @Autowired
    public TradeController(TransactionService transactionService, OfferService offerService, AuthenticationService authenticationService) {
        this.transactionService = transactionService;
        this.offerService = offerService;
        this.authenticationService = authenticationService;
    }

    @CrossOrigin
    @PostMapping("/offer")
    public ResponseEntity<?> storeOneOffer(@RequestBody TradeOfferDto tradeOfferDto, @RequestHeader ("Authorization") String jwt) {
        int IdAccount = authenticationService.getAuthenticatedIdAccount(jwt);
        tradeOfferDto.setIdAccountOffer(IdAccount);
        if (IdAccount != 0) {
            try {
                return new ResponseEntity<>(offerService.tradeOffer(tradeOfferDto), HttpStatus.CREATED);
            } catch (Exception e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @CrossOrigin
    @PostMapping("/bank")
    public ResponseEntity<?> tradeWithBank(@RequestBody TradeBankDto tradeBankDto, @RequestHeader ("Authorization") String jwt) {
        int IdAccount = authenticationService.getAuthenticatedIdAccount(jwt);
        tradeBankDto.setIdAccountTrade(IdAccount);
        if (IdAccount != 0) {
            try {
                return new ResponseEntity<>(transactionService.tradeWithBank(tradeBankDto), HttpStatus.CREATED);
            } catch (Exception e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }



}
