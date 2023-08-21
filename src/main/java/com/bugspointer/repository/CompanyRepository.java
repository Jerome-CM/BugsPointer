package com.bugspointer.repository;

import com.bugspointer.entity.Company;
import com.bugspointer.entity.FirstReport;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends CrudRepository<Company, Long> {

    Optional<Company> findByMail(String mail);

    Optional<Company> findByCompanyName(String companyName);

    Optional<Company> findByPublicKey(String publicKey);

    @Query(value="SELECT COUNT(*) FROM company;", nativeQuery = true)
    Long allCompanyCounted();

    List<Company> findCompaniesByDateCreationBetween(Date dateMini,Date dateMaxi);











        //findViewCountersByDateViewBetween
}
