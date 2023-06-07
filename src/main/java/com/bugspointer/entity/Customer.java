package com.bugspointer.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
@Data
public class Customer {

    @Id
    private String customerId;

    @OneToOne
    private Company company;

    private EnumPlan plan;

    private String dateStartSubscribe;

}
