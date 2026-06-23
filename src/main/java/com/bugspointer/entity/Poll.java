package com.bugspointer.entity;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Entity
public class Poll extends PollUser{

    public static final String CONTEXT_PRODUCT = "PRODUCT";
    public static final String CONTEXT_INSTALLATION = "INSTALLATION";

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String providedBy = "user";

    @Column(columnDefinition = "TEXT")
    private String comment;

    private String pollContext = CONTEXT_PRODUCT;

    private Date dateSend = new Date();

}
