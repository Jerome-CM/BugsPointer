package com.bugspointer.dto;

import lombok.Data;

@Data
public class WidgetConfigDTO {

    private String publicKey;

    private String primaryColor = "#27215F";

    private String buttonText = "Signaler un bug";

    private String title = "Signaler un nouveau bug";

    private String descriptionLabel = "Description du bug";

    private String position = "bottom-right";
}
