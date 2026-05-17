package com.bugspointer.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
public class PlanPricing {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private EnumPlan plan;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal newSubscriptionAmount;

    private Date dateUpdate = new Date();
}
