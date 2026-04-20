package com.maplewood.model;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "semesters")
public class Semester {

    @Id
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "year")
    private Integer year;

    @Column(name = "order_in_year")
    private Integer orderInYear;

    @Column(name = "is_active")
    private Boolean active;

    public String getDisplayName() {
        return name + " " + year;
    }
}
