package com.techpassel.servinja.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name="temp_user")
public class TempUser extends UserFormat{
    @Column(name = "first_name")
    @Size(max = 25)
    @NotNull
    private String firstName;

    @Column(name = "last_name")
    @Size(max = 25)
    @NotNull
    private String lastName;

//    //updatedAt and token should be removed from here and should be implemented using "VerificationToken" entity
//    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
//    LocalDateTime updatedAt;
//
//    @Size(max = 30)
//    @NotNull
//    private String token;

//    TempUser(@Size(max = 25) @NotNull String firstName, @Size(max = 25) @NotNull String lastName, @Size(max = 25) String phone, @Size(max = 40) String email, String password, String roles, @Size(max = 25) @NotNull String token, LocalDateTime dateTime){
//        super(phone,email,password,roles);
//        this.firstName = firstName;
//        this.lastName = lastName;
//        this.token = token;
//        this.updatedAt = dateTime;
//    }


    public TempUser(@Size(max = 25) String phone, @Size(max = 40) String email, String password, String roles, @Size(max = 25) @NotNull String firstName, @Size(max = 25) @NotNull String lastName) {
        super(phone, email, password, roles);
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
