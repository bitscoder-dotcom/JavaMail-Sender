package dev.roman.javamail.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
//    @NaturalId(mutable = true)
    // no two users can have the same id, and a user can change his/her email
    @Column(unique = true)
    private String email;
    private String password;
    private String role;
    private boolean isEnabled = false;
}
