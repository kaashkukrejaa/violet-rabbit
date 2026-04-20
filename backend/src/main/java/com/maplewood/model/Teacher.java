package com.maplewood.model;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "teachers")
public class Teacher {

    @Id
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;
}
