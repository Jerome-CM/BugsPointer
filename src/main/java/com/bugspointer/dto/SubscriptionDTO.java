package com.bugspointer.dto;

import be.woutschoovaerts.mollie.data.common.Amount;
import be.woutschoovaerts.mollie.data.subscription.SubscriptionStatus;
import com.bugspointer.entity.EnumPlan;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SubscriptionDTO {

    private String id;

    private String description;

    private Amount amount;

    private LocalDate nextPaymentDate;

    private String mandateId;

    private SubscriptionStatus status;

    private EnumPlan newPlan;

    private String publicKey;

}
