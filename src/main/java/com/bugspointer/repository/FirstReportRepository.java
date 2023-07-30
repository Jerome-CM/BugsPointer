package com.bugspointer.repository;

import com.bugspointer.entity.FirstReport;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FirstReportRepository extends CrudRepository<FirstReport, Long> {

    /*@Query(value = "SELECT * FROM first_report", nativeQuery = true)
    List<FirstReport> findFirstCandidates();*/

}
