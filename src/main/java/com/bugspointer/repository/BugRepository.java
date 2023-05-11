package com.bugspointer.repository;

import com.bugspointer.entity.Bug;
import com.bugspointer.entity.Company;
import com.bugspointer.entity.EnumEtatBug;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BugRepository extends CrudRepository<Bug, Long> {



    List<Bug> findAllByCompanyAndEtatBug(Company company, EnumEtatBug etatBug);

}
