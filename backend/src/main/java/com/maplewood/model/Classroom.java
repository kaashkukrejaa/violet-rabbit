package com.maplewood.model;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "classrooms")
public class Classroom {

    @Id
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "floor")
    private Integer floor;
}
