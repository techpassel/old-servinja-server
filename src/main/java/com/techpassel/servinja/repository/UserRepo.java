package com.techpassel.servinja.repository;

import com.techpassel.servinja.model.TempUser;
import com.techpassel.servinja.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);

    //Here "1" represent first argument
    @Query("from User where email=?1 or phone=?1")
    Optional<User> findByEmailOrPhone(String username);

    //here "<>" represents "not equal to"
    @Query("from User where phone=?1 and id<>?2")
    Optional<User> findOtherUserByPhone(String phone, int userId);

}
