package com.bugspointer.dto;

import lombok.Data;

@Data
public class WidgetConfigDTO {

    private String publicKey;

    private String primaryColor = "#27215F";

    private String modalBackgroundColor = "#FFFFFF";

    private String modalTextColor = "#24233D";

    private String linkTextColor = "#27215F";

    private boolean linkUnderline = true;

    private String buttonText = "Signaler un bug";

    private String buttonStyle = "button";

    private Integer buttonSize = 56;

    private String title = "Signaler un nouveau bug";

    private String descriptionLabel = "Description du bug";

    private String position = "bottom-right";

    private Integer marginX = 15;

    private Integer marginY = 15;
}
