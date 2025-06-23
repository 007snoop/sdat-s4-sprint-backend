package com.sdat_s4_sprint_backend.entity;

import jakarta.persistence.*;

@Entity
public class Aircraft {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

}
