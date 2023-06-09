package com.bugspointer.entity;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Data
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerId;

    @ToString.Exclude
    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    private EnumPlan plan;

    private String dateStartSubscribe;

    /*@OneToMany
    List<Mandate> mandateList = new ArrayList();*/
    //TODO Class mandate à créer pour que ça fasse aussi une table en bdd
    // ID, customerId mollie, mandateId.

    @Override
    public String toString() {
        return "Customer(" +
                "id=" + id +
                ", customerId=" + customerId +
                ", plan="+ plan +
                ", dateStartSubscribe=" + dateStartSubscribe +
                ", Company(id=" + company.getCompanyId() +
                ", name=" + company.getCompanyName() +
                ", mail=" + company.getMail() +
                "))";
    }
}
