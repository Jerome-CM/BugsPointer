package com.bugspointer.entity;

import lombok.Data;
import javax.persistence.MappedSuperclass;


@Data
@MappedSuperclass
public abstract class PollUser {

    private Integer findEasy;

    private Integer stepClarity;

    private Integer targetFeatureGoodWork;

}
