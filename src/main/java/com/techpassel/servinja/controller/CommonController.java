package com.techpassel.servinja.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.techpassel.servinja.model.Address;
import com.techpassel.servinja.model.Customer;
import com.techpassel.servinja.model.Document;
import com.techpassel.servinja.model.User;
import com.techpassel.servinja.repository.AddressRepo;
import com.techpassel.servinja.repository.CustomerRepo;
import com.techpassel.servinja.repository.DocumentRepo;
import com.techpassel.servinja.repository.UserRepo;
import com.techpassel.servinja.service.AmazonS3Service;
import com.techpassel.servinja.service.OnboardingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:6250")
@RestController
@RequestMapping("/common")
public class CommonController {

    @Autowired
    UserRepo userRepo;

    @Autowired
    AddressRepo addressRepo;

    @Autowired
    DocumentRepo documentRepo;

    @Autowired
    CustomerRepo customerRepo;

    @Autowired
    AmazonS3Service s3Service;

    @Autowired
    OnboardingService onboardingService;

    @PostMapping("/save-address")
    public ResponseEntity<?> saveAddress(@RequestBody ObjectNode objectNode) {
        try {
            int userId = objectNode.get("userId").asInt();
            JsonNode adrJson = objectNode.get("address");
            ObjectMapper mapper = new ObjectMapper();
            Address address = mapper.treeToValue(adrJson, Address.class);
            Optional<User> userIfExist = userRepo.findById(userId);
            String responseType = "error";
            if (userIfExist.isPresent()) {
                User user = userIfExist.get();
                address.setUser(user);
                addressRepo.save(address);
                onboardingService.updateOnboardingStage(userId,3);
                responseType = "success";
            }
            return new ResponseEntity<>(responseType, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.EXPECTATION_FAILED);
        }
    }

    @RequestMapping(value = "get-default-address/{userId}", method = RequestMethod.GET)
    public ResponseEntity<?> getDefaultAddress(@PathVariable("userId") int userId) {
        try {
            Optional<Address> adrIfExist = addressRepo.getDefaultAddressByUserId(userId);
            HashMap<String, Object> h = new HashMap<>();
            h.put("status", "error");
            if (adrIfExist.isPresent()) {
                Address adr = adrIfExist.get();
                h.put("status", "success");
                h.put("address", adr);
            } else {
                h.put("status", "not-found");
            }
            return new ResponseEntity<>(h, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.EXPECTATION_FAILED);
        }
    }

    @PostMapping(value = "/store-user-documents", consumes = "multipart/form-data")
    public ResponseEntity<?> storeUserDocuments(@RequestParam int userId, @RequestParam List<MultipartFile> files, @RequestParam Document.Types docType) {
        try {
            System.out.println("UserId : "+userId);
            System.out.println("DocType : "+docType);
            String responseType = "error";
            Optional<User> userIfExist = userRepo.findById(userId);
            if(userIfExist.isPresent()) {
                User user = userIfExist.get();
                List<Document> docs = new ArrayList<>();
                for (MultipartFile file : files) {
                    String filePath = s3Service.uploadFile(file);
                    System.out.println("filepath : " + filePath);
                    Document doc = new Document(docType,filePath,user);
                    //Temporarily making property "isDocVerified" as true from here itself but later it needs to be changed.
                    //Admin should explicitly verify the document and then set "isDocVerified" property as true.
                    doc.setDocVerified(true);
                    docs.add(doc);
                }
                if (docs.size()>0){
                    documentRepo.saveAll(docs);
                    onboardingService.updateOnboardingStage(userId,4);
                    //Temporarily making property "isDocVerified" as true from here itself but later it needs to be changed.
                    //Admin should explicitly verify the document and then set "isDocVerified" property as true.
                    onboardingService.updateCustomerDocVerificationStatus(userId, true);
                    responseType = "success";
                }
            }
            return new ResponseEntity<>(responseType, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.EXPECTATION_FAILED);
        }
    }
}
