package com.ddoddii.resume.model;

import com.ddoddii.resume.model.eunm.LoginType;
import com.ddoddii.resume.model.eunm.RoleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "user_id")
        })
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class User extends BaseEntity {
    private static final Integer REMAIN_INTERVIEW = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "password")
    private String password;

    @Column(name = "name")
    private String name;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "remain_interview")
    private Integer remainInterview;

    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    @Enumerated(EnumType.STRING)
    @NotNull
    private RoleType role;

    @OneToMany(mappedBy = "user")
    private List<Resume> resumes;

    @OneToMany(mappedBy = "user")
    private List<Interview> interviews;

    public static User signUpUser(String name, String email, String encryptedPassword){
        return User.builder()
                .name(name)
                .email(email)
                .password(encryptedPassword)
                .loginType(LoginType.EMAIL)
                .role(RoleType.ROLE_USER)
                .remainInterview(REMAIN_INTERVIEW)
                .build();
    }

}