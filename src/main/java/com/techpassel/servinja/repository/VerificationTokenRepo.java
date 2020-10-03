package com.techpassel.servinja.repository;

import com.techpassel.servinja.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.Optional;

public interface VerificationTokenRepo extends JpaRepository<VerificationToken, Integer> {
    Optional<VerificationToken> findByToken(String token);

    @Query("from VerificationToken where token=?1 and userId=?2")
    Optional<VerificationToken> findByTokenAndUserId(String token, int userId);

    //@Transactional And @Modifying annotations are required if you want to update value.
    @Transactional
    @Modifying
    @Query("delete from VerificationToken v where v.userId=?1 and v.type=?2")
    void deleteByUserIdAndType(int userId, VerificationToken.Types type);
}
