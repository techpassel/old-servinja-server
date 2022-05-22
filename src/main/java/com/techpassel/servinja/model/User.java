package com.techpassel.servinja.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "user")
public class User extends UserFormat {
    @Column(name = "is_Active")
    @Size(max = 1)
    private boolean active;

    User(@Size(max = 25) String phone,  @NotNull @Size(max = 40) String email, @Size(max = 1) boolean active, String password, String roles) {
        super(phone, email, password, roles);
        this.active = active;
    }
}
