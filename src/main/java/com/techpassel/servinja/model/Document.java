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
@Table(name = "document")
public class Document {
    public static enum Types {
        IdentityProof,
        AddressProof,
        Other
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    @Column(name = "doc_type")
    private Types docType;

    @NotNull
    private String path;

    @NotNull
    @Column(name = "is_doc_verified")
    private boolean isDocVerified;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="user_id", nullable=false)
    @JsonIgnoreProperties(value = {"applications", "hibernateLazyInitializer"})
    private User user;

    public Document(@NotNull Types docType, @NotNull String path, User user) {
        this.docType = docType;
        this.path = path;
        this.user = user;
        this.isDocVerified = false;
    }
}
