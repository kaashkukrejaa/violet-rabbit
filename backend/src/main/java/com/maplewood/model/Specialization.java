package com.maplewood.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "specializations")
public class Specialization {

    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}
