package com.bugspointer.repository;

import com.bugspointer.entity.Bug;
import com.bugspointer.entity.Company;
import com.bugspointer.entity.EnumEtatBug;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface BugRepository extends CrudRepository<Bug, Long> {


    //Liste des bugs selon la company et l'état du bug (new, pending, solved, ignored)
    List<Bug> findAllByCompanyAndEtatBug(Company company, EnumEtatBug etatBug);

    //Liste des bugs selon la company
    List<Bug> findAllByCompany(Company company);

    @Query(value = "SELECT date_creation FROM bug WHERE adresse_ip = :adresseIp ORDER BY date_creation DESC LIMIT 1", nativeQuery = true)
    Optional<Date> findLastDateCreationByAdresseIp(@Param("adresseIp") String adresseIp);

    @Query(value="SELECT COUNT(*) FROM bug;", nativeQuery = true)
    Long allBugCounted();

}
