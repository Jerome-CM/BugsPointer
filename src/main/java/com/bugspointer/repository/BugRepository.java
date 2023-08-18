package com.bugspointer.repository;

import com.bugspointer.entity.Bug;
import com.bugspointer.entity.Company;
import com.bugspointer.entity.EnumEtatBug;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BugRepository extends CrudRepository<Bug, Long> {


    //Liste des bugs selon la company et l'état du bug (new, pending, solved, ignored)
    List<Bug> findAllByCompanyAndEtatBug(Company company, EnumEtatBug etatBug);

    //Liste des bugs selon la company
    List<Bug> findAllByCompany(Company company);

    //Liste des bugs selon l'adresse Ip
    List<Bug> findAllByAdresseIp(String adresseIp);

    @Query(value="SELECT COUNT(*) FROM bug;", nativeQuery = true)
    Long allBugCounted();

}
