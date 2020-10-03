package com.techpassel.servinja.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NoArgsConstructor @Getter @Setter
@MappedSuperclass
public class UserFormat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    @Size(max = 40)
    private String email;

    @Size(max = 25)
    private String phone;

    private String password;
    private String roles;

    public UserFormat(@Size(max = 25) String phone, @Size(max = 40) String email, String password, String roles) {
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.roles = roles;
    }
}
