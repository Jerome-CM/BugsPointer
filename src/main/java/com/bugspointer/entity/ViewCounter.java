package com.bugspointer.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name="counter")
public class ViewCounter {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private EnumViewCounterPage page;

    @Temporal(TemporalType.DATE)
    private Date dateView = new Date();

    private String adresseIp;

    public ViewCounter(EnumViewCounterPage page, Date dateView) {
        this.page = page;
        this.dateView = dateView;
    }

    public ViewCounter(EnumViewCounterPage page, Date dateView, String adresseIp) {
        this.page = page;
        this.dateView = dateView;
        this.adresseIp = adresseIp;
    }

    public ViewCounter() {

    }
}
