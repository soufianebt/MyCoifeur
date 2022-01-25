package com.mycoiffeur.controllers;

import com.mycoiffeur.modele.*;
import com.mycoiffeur.repository.ClientRepo;
import com.mycoiffeur.repository.CoiffureRepo;
import com.mycoiffeur.repository.ProfileRepo;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.Optional;

@RestController
public class UserController {
    @Autowired
    private CoiffureRepo coiffureRepo;
    @Autowired
    private ClientRepo clientRepo;
    @Autowired
    private ProfileRepo profileRepo;

        Logger logger =  LoggerFactory.getLogger(UserController.class);


    @RequestMapping("/")
    public String firstPage() {
        return "Hello " +
                "Go To <a href=\"./swagger-ui.html\"> Link </a> To see documentation ";
    }

    @PostMapping(value = "/SignUp")
    public ResponseEntity<String> singUp(@RequestBody User user){
        try{
            Optional<Coiffure> coiffure = Optional.ofNullable(coiffureRepo.findByEmail(user.getEmail()).orElse(null));
            Optional<Client> client = Optional.ofNullable(clientRepo.findByEmail(user.getEmail()).orElse(null));
            String userId = generateUserId();
            if(coiffure.isPresent() || client.isPresent()){
                        return new ResponseEntity<>("user already exist",HttpStatus.ALREADY_REPORTED);
            }
            if(user.getUserType().equals("Client")){
                clientRepo.save(new Client(userId, user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassWord(), user.getAddress(),user.getUserType(),0f, 0f));
            }else if(user.getUserType().equals("Coiffure")){
                coiffureRepo.save(new Coiffure(userId, user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassWord(), user.getAddress(),user.getUserType(),false, false));
                Profile profile = new Profile();
                profile.setUserId(userId);
                profileRepo.save(profile);
            }else{
                logger.info("Unspecified type Coiffure, Client");
                return new ResponseEntity<>("Unspecified type Coiffure, Client",HttpStatus.EXPECTATION_FAILED);
            }
            logger.info("Created Successfully");
        return new ResponseEntity<>("Created Successfully",HttpStatus.OK);
    }catch (Exception e){
            logger.error(e.toString());
            return new ResponseEntity<>(e.toString(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/SignUp/{email}")
    public ResponseEntity<User> singUp(@PathVariable String email) {
        try {
            Optional<Client> client = Optional.ofNullable(clientRepo.findByEmail(email).orElse(null));
            Optional<Coiffure> coiffure = Optional.ofNullable(coiffureRepo.findByEmail(email).orElse(null));
            if (client.isPresent()) {
                logger.info("find User client");
                Client modifiedClient = client.get();
                modifiedClient.setPassWord("");
                return new ResponseEntity<>(modifiedClient, HttpStatus.OK);
            }else if(coiffure.isPresent()){
                logger.info("find User Coiffure");
                Coiffure modifiedCoiffure = coiffure.get();
                modifiedCoiffure.setPassWord("");
                return new ResponseEntity<>(modifiedCoiffure, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            logger.error(e.toString());
            return new ResponseEntity<>( HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/SignIn")
    public ResponseEntity<User> signIn(@RequestBody LoginIdentifier loginIdentifier){

        try {
            Optional<Client> client = Optional.ofNullable(clientRepo.findByEmail(loginIdentifier.getEmail()).orElse(null));
            Optional<Coiffure> coiffure = Optional.ofNullable(coiffureRepo.findByEmail(loginIdentifier.getEmail()).orElse(null));
            if (client.isPresent()) {
                if(loginIdentifier.getPassWord().equals(client.get().getPassWord())){
                    logger.info("Client found");
                    Client modifiedClient = client.get();
                    modifiedClient.setPassWord("");
                    return new ResponseEntity<>(modifiedClient, HttpStatus.OK);
                }else{
                    logger.warn("Client Unauthorized");
                    return new ResponseEntity<>( HttpStatus.UNAUTHORIZED);
                }
            }else if(coiffure.isPresent()){
                if(loginIdentifier.getPassWord().equals(coiffure.get().getPassWord())) {
                    logger.info("coiffure found");
                    Coiffure modifiedCoiffure = coiffure.get();
                    modifiedCoiffure.setPassWord("");
                    return new ResponseEntity<>(modifiedCoiffure, HttpStatus.OK);
                }else{
                    logger.warn("Client Unauthorized");
                    return new ResponseEntity<>( HttpStatus.UNAUTHORIZED);
                }
            }
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            logger.error(e.toString());
            return new ResponseEntity<>( HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping(value = "/UserUpdate")
    public ResponseEntity<String> UpdateUser(@RequestBody User user) {
        try {
            if(user.getUserId() == null){
                return new ResponseEntity<>("You must Specified UserId",HttpStatus.NO_CONTENT);
            }

            Optional<Coiffure> coiffure = Optional.ofNullable(coiffureRepo.findById(user.getUserId()).orElse(null));
            Optional<Client> client = Optional.ofNullable(clientRepo.findById(user.getUserId()).orElse(null));
            if (coiffure.isPresent() || client.isPresent()) {
                if(user.getUserType().equals("Client")){
                    logger.info("Updated Successfully");
                    clientRepo.save(new Client(user.getUserId(), user.getFirstName(), user.getLastName(), user.getEmail(), client.get().getPassWord(), user.getAddress(),user.getUserType(),0f, 0f));
                    return new ResponseEntity<>("Updated Successfully",HttpStatus.OK);
                }else if(user.getUserType().equals("Coiffure")){
                    logger.info("Updated Successfully");
                    coiffureRepo.save(new Coiffure(user.getUserId(), user.getFirstName(), user.getLastName(), user.getEmail(), coiffure.get().getPassWord(), user.getAddress(),user.getUserType(),false, false));
                    return new ResponseEntity<>("Updated Successfully",HttpStatus.OK);
                }else{
                    logger.info("Specified type Coiffure, Client");
                    return new ResponseEntity<>("Specified type Coiffure, Client",HttpStatus.NOT_FOUND);
                }
            }
            return new ResponseEntity<>("Client Not found",HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error(e.toString());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String generateUserId(){
       return  RandomStringUtils.random(20, 0, 0, true, true, null, new SecureRandom());
    }
}

