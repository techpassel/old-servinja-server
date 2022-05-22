package com.techpassel.servinja.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "verification_token")
public class VerificationToken {
    public static enum Types {
        EmailVerificationToken,
        PhoneVerificationToken,
        EmailUpdationToken,
        ChangePasswordToken
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    private int userId;

    @NotNull
    @Size(max = 30)
    private String token;

    @NotNull
    @Size(max = 30)
    private Types type;

    @Column(name = "generated_at", columnDefinition = "TIMESTAMP")
    LocalDateTime generatedAt;

    public VerificationToken(@NotNull int userId, @NotNull @Size(max = 30) String token, @NotNull @Size(max = 30) Types type, LocalDateTime generatedAt) {
        this.userId = userId;
        this.token = token;
        this.type = type;
        this.generatedAt = generatedAt;
    }
}
