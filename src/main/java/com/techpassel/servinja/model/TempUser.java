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
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
    LocalDateTime updatedAt;

    @Size(max = 30)
    @NotNull
    private String token;

    TempUser(@Size(max = 25) @NotNull String firstName, @Size(max = 25) @NotNull String lastName, @Size(max = 25) String phone, @Size(max = 40) String email, @Size(max = 1) boolean active, String password, String roles, @Size(max = 25) @NotNull String token, LocalDateTime dateTime){
        super(firstName,lastName,phone,email,active,password,roles);
        this.token = token;
        this.updatedAt = dateTime;
    }
}
