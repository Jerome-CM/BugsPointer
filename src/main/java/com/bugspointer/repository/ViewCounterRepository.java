package com.bugspointer.repository;

import com.bugspointer.entity.ViewCounter;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ViewCounterRepository extends CrudRepository<ViewCounter, Long> {

    List<ViewCounter> findViewCountersByDateViewBetween(Date dateView, Date dateView2);

}
