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

    public ViewCounter(EnumViewCounterPage page, Date dateView) {
        this.page = page;
        this.dateView = dateView;
    }

    public ViewCounter() {

    }
}
