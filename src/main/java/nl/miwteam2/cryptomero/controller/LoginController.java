package nl.miwteam2.cryptomero.controller;

import nl.miwteam2.cryptomero.domain.UserAccount;
import nl.miwteam2.cryptomero.service.Authentication.AuthenticationService;
import nl.miwteam2.cryptomero.service.Authentication.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author MinkTK
 * @version 1.2
 */

@RestController
@RequestMapping("/login")
public class LoginController {

    private final LoginService loginService;
    private final AuthenticationService authenticationService;

    private final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    public LoginController(LoginService loginService, AuthenticationService authenticationService) {
        this.loginService = loginService;
        this.authenticationService = authenticationService;
    }

    // ROUTE with credentials in AUTHORIZATION HEADER
    @PostMapping
    public ResponseEntity<String> login(@RequestHeader ("Authorization") String credentials) throws Exception {
        logger.info("new login with header attempt");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("accessToken", loginService.login(credentials));
            return new ResponseEntity<>("Login Succesful", headers, HttpStatus.OK);
        } catch (Exception exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

//    MTK: ROUTE with credentials in REQUEST BODY
    @PostMapping("/body")
    public ResponseEntity<String> login(@RequestBody UserAccount userAccount) {
        logger.info("new login with body attempt");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("accessToken", loginService.login(userAccount));
            return new ResponseEntity<>("Login Succesful", headers, HttpStatus.OK);
        } catch (Exception exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }



//    // MTK test methode voor authentication purpose - to be continued
    @GetMapping
    public ResponseEntity<String> statusLogin(@RequestHeader ("Authorization") String jwt) throws Exception {
        System.out.println(jwt);
        if (authenticationService.authenticateToken(jwt)) {
            return new ResponseEntity<>("ingelogd", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("niet ingelogd", HttpStatus.UNAUTHORIZED);
        }
    }

}
