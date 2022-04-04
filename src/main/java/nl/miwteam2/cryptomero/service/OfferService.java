package nl.miwteam2.cryptomero.service;

import nl.miwteam2.cryptomero.domain.*;
import nl.miwteam2.cryptomero.repository.CustomerRepository;
import nl.miwteam2.cryptomero.repository.OfferDao;
import nl.miwteam2.cryptomero.repository.OfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * checked offer op match en vraagt eventueel transactie aan
 * @author: Samuel Geurts en Stijn Klijn
 * Version 1.2
*/
@Service
public class OfferService {

    private CustomerService customerService;
    private AssetService assetService;
    private CustomerRepository customerRepository;
    private TransactionService transactionService;
    private OfferRepository offerRepository;

    @Autowired
    public OfferService(CustomerService customerService,AssetService assetService, CustomerRepository customerRepository, TransactionService transactionService, OfferRepository offerRepository) {
        this.customerService = customerService;
        this.assetService = assetService;
        this.customerRepository = customerRepository;
        this.transactionService = transactionService;
        this.offerRepository = offerRepository;
    }

    //methode die alles afhandelt
    public int tradeOffer(TradeOfferDto tradeOfferDto) throws Exception {

        System.out.println(0);
        Offer offer = getOffer(tradeOfferDto);
        boolean isSeller = offer.getAmountOffer() > 0;
        int numberOfPerformedTransactions = 0;

        System.out.println(1);

        //verkopen of kopen
        Offer offerClone = new Offer(offer); //to prevent changes to this offer
        List<Offer> matchList = isSeller? findBuyers(offerClone):findSellers(offerClone);

        for (Offer counterOffer: matchList){
            //todo maak Transaction met informatie offer en counteroffer;
            TransactionDto transactionDto;

            if (isSeller) {
                transactionDto = new TransactionDto(LocalDateTime.now(), tradeOfferDto.getCustomer(),
                        customerRepository.findById(counterOffer.getUserOffer().getIdAccount()),
                        counterOffer.getAssetOffer(),counterOffer.getAmountOffer(),counterOffer.getPriceOffer());
            } else {
                transactionDto = new TransactionDto(LocalDateTime.now(),
                        customerRepository.findById(counterOffer.getUserOffer().getIdAccount()), tradeOfferDto.getCustomer(),
                        counterOffer.getAssetOffer(),counterOffer.getAmountOffer(),counterOffer.getPriceOffer());
            }
            //todo spreek TransactionService aan en doe transactie attempt
            System.out.println(2);
            try {
                Transaction transaction = transactionService.tradeWithUser(transactionDto);
            } catch (Exception e) {
                //todo als het niet lukt - break - sla offer op in offerTabel
                System.out.println(e.getMessage());
                int offerId = offerRepository.storeOne(offer);
                System.out.println(matchList.toString());
                throw e;
            }

            //todo als het wel lukt - offertabel updaten - offer model updaten

            /**
             * Update offer tabel(entry counter offer) als de koop/verkoop partitieel is
             * Delete offer tabel(entry counter offer) als de koop/verkoop compleet is
             * */
            Offer storedCounterOffer  = offerRepository.findById(counterOffer.getIdOffer());
            if (storedCounterOffer.getAmountOffer() != counterOffer.getAmountOffer()){
                storedCounterOffer.setAmountOffer(storedCounterOffer.getAmountOffer() - counterOffer.getAmountOffer());
                offerRepository.updateOne(storedCounterOffer);
            } else {
                offerRepository.deleteOne(storedCounterOffer.getIdOffer());
            }
            System.out.println(3);
            /**
             * Update offer voor gebruik in de loop
             * omdat voor offer en counterOffer het getAmountOffer() een tegengesteld sign heeft moet een optelling plaats vinden.
             */
            offer.setAmountOffer(offer.getAmountOffer() + counterOffer.getAmountOffer());
            numberOfPerformedTransactions++;
            System.out.println(offer);
        }

        if (offer.getAmountOffer() != 0) {
            //todo schrijf offer naar database
            offerRepository.storeOne(offer);
        }

        //todo iets terug geven
        return numberOfPerformedTransactions;
    }

    //maakt een offer van trade Offer Dto
    public Offer getOffer(TradeOfferDto tradeOfferDto) {
        Offer offer = new Offer(tradeOfferDto);
        Asset asset = assetService.findByName(tradeOfferDto.getAssetNameOffer());
        offer.setUserOffer(tradeOfferDto.getCustomer());
        offer.setAssetOffer(asset);
        offer.setTimestampOffer(new Timestamp(System.currentTimeMillis()));
        return offer;
    }

    public List<Offer> getAll() {
        //todo haal alle offers op uit de database
        return offerRepository.getAll();
    }

    public List<Offer> findSellers(Offer buyer) {
        List<Offer> sellers = getAll().stream().filter(e -> e.getAssetOffer().equals(buyer.getAssetOffer()) && e.getAmountOffer() > 0)
                .sorted(Comparator.comparing(Offer::getPriceOffer)).toList();
        buyer.setAmountOffer(-1 * buyer.getAmountOffer());

        List<Offer> matches = new ArrayList<>();

        for (Offer seller : sellers) {
            if (buyer.getAmountOffer() > 0 && seller.getAmountOffer() > 0 && seller.getPriceOffer() <= buyer.getPriceOffer()) {
                double number = Math.min(seller.getAmountOffer(), buyer.getAmountOffer());
                matches.add(new Offer(seller.getIdOffer(), seller.getUserOffer(), seller.getAssetOffer(), number, seller.getPriceOffer(), seller.getTimestampOffer()));
                seller.setAmountOffer(seller.getAmountOffer() - number);
                buyer.setAmountOffer(buyer.getAmountOffer() - number);
            }
        }
        return matches;
    }

    public List<Offer> findBuyers(Offer seller) {
        List<Offer> buyers = getAll().stream().filter(e -> e.getAssetOffer().equals(seller.getAssetOffer()) && e.getAmountOffer() < 0)
                .sorted(Comparator.comparing(Offer::getPriceOffer).reversed()).toList();
        buyers.forEach(e -> e.setAmountOffer(-1 * e.getAmountOffer()));

        List<Offer> matches = new ArrayList<>();

        for (Offer buyer : buyers) {
            if (buyer.getAmountOffer() > 0 && seller.getAmountOffer() > 0 && seller.getPriceOffer() <= buyer.getPriceOffer()) {
                double number = Math.min(seller.getAmountOffer(), buyer.getAmountOffer());
                matches.add(new Offer(buyer.getIdOffer(), buyer.getUserOffer(), buyer.getAssetOffer(), number, buyer.getPriceOffer(), buyer.getTimestampOffer()));
                seller.setAmountOffer(seller.getAmountOffer() - number);
                buyer.setAmountOffer(buyer.getAmountOffer() - number);
            }
        }
        return matches;
    }
}
