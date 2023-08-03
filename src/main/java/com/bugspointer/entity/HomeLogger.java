package com.bugspointer.entity;

import com.bugspointer.entity.enumLogger.Action;
import com.bugspointer.entity.enumLogger.Adjective;
import com.bugspointer.entity.enumLogger.Raison;
import com.bugspointer.entity.enumLogger.What;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Entity
@Table(name="logger")
@Component
public class HomeLogger {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long companyId;

    @NotNull
    private Action action;

    @NotNull
    private What what;

    private String identifier;

    private Adjective adjective;

    private Raison raison;

    @NotNull
    @JsonFormat(pattern = "dd/mm/YYYY HH:ii:ss")
    private Date dateLog = new Date();

     public HomeLogger() {
    }


}
