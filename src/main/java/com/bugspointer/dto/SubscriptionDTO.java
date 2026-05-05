package com.bugspointer.dto;

import be.woutschoovaerts.mollie.data.common.Amount;
import be.woutschoovaerts.mollie.data.subscription.SubscriptionStatus;
import com.bugspointer.entity.EnumPlan;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class SubscriptionDTO {

    public interface Change {}

    @NotBlank(message = "L'abonnement actuel est introuvable", groups = Change.class)
    private String id;

    private String description;

    private Amount amount;

    private LocalDate nextPaymentDate;

    @NotBlank(message = "Le mandat est obligatoire", groups = Change.class)
    private String mandateId;

    @NotNull(message = "Le statut de l'abonnement est obligatoire", groups = Change.class)
    private SubscriptionStatus status;

    @NotNull(message = "Le nouveau plan est obligatoire", groups = Change.class)
    private EnumPlan newPlan;

    private String publicKey;

}
