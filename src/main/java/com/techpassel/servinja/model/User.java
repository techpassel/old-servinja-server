package com.techpassel.servinja.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name="user")
public class User extends UserFormat{
    User(@Size(max = 25) @NotNull String firstName, @Size(max = 25) @NotNull String lastName, @Size(max = 25) String phone, @Size(max = 40) String email, @Size(max = 1) boolean active, String password, String roles){
        super(firstName,lastName,phone,email,active,password,roles);
    }
}
