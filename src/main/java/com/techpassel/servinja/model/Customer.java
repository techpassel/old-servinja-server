package com.techpassel.servinja.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    //Unidirectional one to one relationship.Nothing required to do in User entity
    @OneToOne(targetEntity = User.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "first_name")
    @Size(max = 25)
    @NotNull
    private String firstName;

    @Column(name = "last_name")
    @Size(max = 25)
    @NotNull
    private String lastName;

    @Column(name = "onboarding_stage")
    private int onboardingStage;
    //(onboardingStage = 0) means all stages are completed.

    @Column(name = "age")
    private int age;

    @Column(name = "gender")
    private String gender;

    @Column(name = "doc_location")
    private String docLocation;

    @Column(name = "is_doc_verified")
    private boolean isDocVerified;

    @Column(name = "is_email_verified")
    private boolean isEmailVerified;

    @Column(name = "is_phone_verified")
    private boolean isPhoneVerified;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    //As address will be there for any type of users so added it here instead of Customer entity.
//    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy="user")
//    List<Address> address;

    public Customer(@Size(max = 25) @NotNull String firstName, @Size(max = 25) @NotNull String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.onboardingStage = 1;
        this.age = 0;
        // Making isEmailVerified = true as new customer will be created after email verification only.
        this.isEmailVerified = true;
        this.isPhoneVerified = false;
        this.isDocVerified = false;
    }
}
