package com.bugspointer.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String customerId;

    @OneToOne
    private Company company;

    private EnumPlan plan;

    private String dateStartSubscribe;

}
