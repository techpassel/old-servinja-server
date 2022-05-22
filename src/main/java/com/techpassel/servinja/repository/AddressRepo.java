package com.techpassel.servinja.repository;

import com.techpassel.servinja.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AddressRepo extends JpaRepository<Address, Integer> {

    @Query("from Address where user_id=?1 and isDefault=true")
    Optional<Address> getDefaultAddressByUserId(int userId);
}
