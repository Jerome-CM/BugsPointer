package com.bugspointer.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;

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

    @OneToMany
    List<Mandate> mandateList = new ArrayList();
    //TODO Class mandate à créer pour que ça fasse aussi une table en bdd
    // ID, customerId mollie, mandateId.
}
