package com.techpassel.servinja.repository;

import com.techpassel.servinja.model.TempUser;
import com.techpassel.servinja.model.User;
import com.techpassel.servinja.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    Optional<User> findByRoles(String role);

    //Here "1" represent first argument
    @Query("from User where email=?1 or phone=?1")
    Optional<User> findByEmailOrPhone(String username);

    //Here "<>" represents "not equal to"
    @Query("from User where phone=?1 and id<>?2")
    Optional<User> findOtherUserByPhone(String phone, int userId);

    //@Transactional And @Modifying annotations are required if you want to update value.
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update User u set u.password = :password where u.id=:userId")
    void updatePassword(int userId, String password);

}
