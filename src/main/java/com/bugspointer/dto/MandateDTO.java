package com.bugspointer.dto;

import be.woutschoovaerts.mollie.data.mandate.MandateStatus;
import lombok.Data;

@Data
public class MandateDTO {

    private String mandateId;

    private String signatureDate;

    private String status;

}
