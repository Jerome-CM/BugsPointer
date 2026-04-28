package com.bugspointer.entity;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Data
public class AdminBilling {

    @Id
    @GeneratedValue
    private Long id;

    private String label;

    private String category;

    private BigDecimal amount;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate billingDate;

    private Date dateCreation = new Date();
}
