package com.techpassel.servinja.service;

import com.techpassel.servinja.model.Customer;
import com.techpassel.servinja.repository.CustomerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OnboardingService {

    @Value("${totalOnboardingStage}")
    public int totalOnboardingStage;

    @Autowired
    CustomerRepo customerRepo;

    public boolean updateOnboardingStage(int userId, int currentOnboardingStage) {
        Optional<Customer> customerIfExist = customerRepo.findByUserId(userId);
        if (customerIfExist.isPresent()) {
            Customer customer = customerIfExist.get();
            if (customer.getOnboardingStage() == currentOnboardingStage) {
                int newOnboardingStage = currentOnboardingStage == totalOnboardingStage ? 0 : ++currentOnboardingStage;
                customer.setOnboardingStage(newOnboardingStage);
            }
            customer.setPhoneVerified(true);
            customerRepo.save(customer);
            return true;
        } else {
            return false;
        }
    }

    public boolean updateCustomerDocVerificationStatus(int userId, boolean status){
        Optional<Customer> customerIfExist = customerRepo.findByUserId(userId);
        if (customerIfExist.isPresent()) {
            Customer customer = customerIfExist.get();
            customer.setDocsVerified(status);
            customerRepo.save(customer);
            return true;
        } else {
            return false;
        }
    }

}
