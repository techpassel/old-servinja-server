package com.techpassel.servinja.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.techpassel.servinja.model.Address;
import com.techpassel.servinja.model.User;
import com.techpassel.servinja.repository.AddressRepo;
import com.techpassel.servinja.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:6250")
@RestController
@RequestMapping("/common")
public class CommonController {

    @Autowired
    UserRepo userRepo;

    @Autowired
    AddressRepo addressRepo;

    @PostMapping("/save-address")
    public ResponseEntity<?> saveAddress(@RequestBody ObjectNode objectNode){
        try{
            int userId = objectNode.get("userId").asInt();
            JsonNode adrJson = objectNode.get("address");
            ObjectMapper mapper = new ObjectMapper();
            Address address = mapper.treeToValue(adrJson, Address.class);
            Optional<User> userIfExist = userRepo.findById(userId);
            String responseType = "error";
            if(userIfExist.isPresent()){
                User user = userIfExist.get();
                address.setUser(user);
                addressRepo.save(address);
                responseType = "success";
            }
            return new ResponseEntity<>(responseType, HttpStatus.OK);
        } catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.EXPECTATION_FAILED);
        }
    }

    @RequestMapping(value = "get-default-address/{userId}", method = RequestMethod.GET)
    public ResponseEntity<?> getDefaultAddress(@PathVariable("userId") int userId) {
        try{
            Optional<Address> adrIfExist = addressRepo.getDefaultAddressByUserId(userId);
            HashMap<String, Object> h = new HashMap<>();
            h.put("status", "error");
            if(adrIfExist.isPresent()){
                Address adr = adrIfExist.get();
                h.put("status","success");
                h.put("address", adr);
            } else {
                h.put("status","not-found");
            }
            return new ResponseEntity<>(h, HttpStatus.OK);
        } catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.EXPECTATION_FAILED);
        }
    }
}
