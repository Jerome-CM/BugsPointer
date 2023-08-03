package com.bugspointer.repository;

import com.bugspointer.entity.FirstReport;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FirstReportRepository extends CrudRepository<FirstReport, Long> {

    @Query(value = "SELECT fr.id, c.company_name, c.domaine, fr.date_confirm, fr.first_report, fr.first_description, fr.first_send, fr.second_report, fr.second_description, fr.second_send, c.domaine,c.company_id FROM first_report AS fr INNER JOIN company AS c ON c.company_id=fr.company_id WHERE fr.first_report = false AND c.date_download BETWEEN :dateMini AND :dateMaxi", nativeQuery = true)
    List<FirstReport> findFirstCandidates(@Param("dateMini") String dateMini, @Param("dateMaxi") String dateMaxi);

    @Query(value = "SELECT fr.id, c.company_name, c.domaine, fr.date_confirm, fr.first_report, fr.first_description, fr.first_send, fr.second_report, fr.second_description, fr.second_send, c.domaine,c.company_id FROM first_report AS fr INNER JOIN company AS c ON c.company_id=fr.company_id WHERE fr.first_report = true AND fr.second_report = false AND c.date_download BETWEEN :dateMini AND :dateMaxi", nativeQuery = true)
    List<FirstReport> findSecondCandidates(@Param("dateMini") String dateMini, @Param("dateMaxi") String dateMaxi);


}
