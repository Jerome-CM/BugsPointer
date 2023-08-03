package com.bugspointer.repository;

import com.bugspointer.entity.HomeLogger;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface HomeLoggerRepository extends CrudRepository<HomeLogger, Long> {

    List<HomeLogger> findAllByCompanyId(Long id);

}
