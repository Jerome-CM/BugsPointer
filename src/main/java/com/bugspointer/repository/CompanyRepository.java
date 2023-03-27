package com.bugspointer.repository;

import com.bugspointer.entity.Compagny;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompagnyRepository extends CrudRepository<Compagny, Long> {



}
