package com.techpassel.servinja.repository;

import com.techpassel.servinja.model.TempUser;
import com.techpassel.servinja.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
@Transactional
public interface TempUserRepo extends JpaRepository<TempUser, Integer> {
    Optional<TempUser> findByEmail(String email);
    Optional<TempUser> findByPhone(String phone);
    void deleteAllByEmail(String email);
    void deleteAllByPhone(String phone);
    Optional<TempUser> findByToken(String token);

    @Query("from TempUser where email=?1 or phone=?1")
    Optional<TempUser> findByEmailOrPhone(String username);

    @Modifying
    @Query("delete from TempUser u where u.email=?1 or u.phone=?2")
    void deleteAllByEmailAndPhone(String email, String phone);
    //Here "?1" represents 1st argument, similarly if it had second argument you could have used it like "id=?2"
}
