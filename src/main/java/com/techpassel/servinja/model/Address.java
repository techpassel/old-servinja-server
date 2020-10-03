package com.techpassel.servinja.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "address")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    private String type;

    @NotNull
    private String address;

    @NotNull
    private String state;

    @NotNull
    private String district;

    @NotNull
    private int pin;

    @NotNull
    private String city;

    @NotNull
    private String landmark;

    @NotNull
    @Column(name = "is_default")
    private boolean isDefault;

    private String latitude;

    private String longitude;

    private String location;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="user_id", nullable=false)
    @JsonIgnoreProperties(value = {"applications", "hibernateLazyInitializer"})
    private User user;
}
