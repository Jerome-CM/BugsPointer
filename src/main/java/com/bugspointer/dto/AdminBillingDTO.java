package com.bugspointer.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AdminBillingDTO {

    @NotBlank(message = "Le libellé est obligatoire")
    private String label;

    @NotBlank(message = "La catégorie est obligatoire")
    private String category;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    private BigDecimal amount;

    @NotNull(message = "La date est obligatoire")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate billingDate;
}
