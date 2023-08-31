package com.bugspointer.dto;

import be.woutschoovaerts.mollie.data.mandate.MandateStatus;
import lombok.Data;

@Data
public class MandateDTO {

    private String mandateId;

    private String customerId;

    private String signatureDate;

    private String validDate;

    private String status;

    private String iban;

    private String bic;

}
