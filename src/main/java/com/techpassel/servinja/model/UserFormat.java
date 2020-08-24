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

    @Column(name = "first_name")
    @Size(max = 25)
    @NotNull
    private String firstName;

    @Column(name = "last_name")
    @Size(max = 25)
    @NotNull
    private String lastName;

    @Size(max = 25)
    private String phone;

    @Column(unique = true, nullable = false)
    @Size(max = 40)
    private String email;

    @Column(name="is_Active")
    @Size(max = 1)
    private boolean active;
    private String password;
    private String roles;

    public UserFormat(@Size(max = 25) @NotNull String firstName, @Size(max = 25) @NotNull String lastName, @Size(max = 25) String phone, @Size(max = 40) String email, @Size(max = 1) boolean active, String password, String roles) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.active = active;
        this.password = password;
        this.roles = roles;
    }
}
