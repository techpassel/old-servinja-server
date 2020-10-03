package com.techpassel.servinja.repository;

import com.techpassel.servinja.model.Customer;
import com.techpassel.servinja.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface CustomerRepo extends JpaRepository<Customer, Integer> {
    @Query("from Customer where user_id=?1")
    Optional<Customer> findByUserId(int userId);

    //@Transactional
    //@Modifying
    //@Query("update Customer set onboardingStage=?1 where userId=?2")
    //void updateOnboardingStageByUserId(int newStage, int userId);


}
