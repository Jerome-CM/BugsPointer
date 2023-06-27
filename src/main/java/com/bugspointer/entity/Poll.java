package com.bugspointer.entity;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Entity
public class Poll extends PollUser{

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String providedBy = "user";

    @Column(columnDefinition = "TEXT")
    private String comment;

    private Date dateSend = new Date();

}
