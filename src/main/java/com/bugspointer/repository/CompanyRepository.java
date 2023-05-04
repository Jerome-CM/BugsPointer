package com.bugspointer.repository;

import com.bugspointer.entity.Company;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends CrudRepository<Company, Long> {

    Optional<Company> findByMail(String mail);

    Optional<Company> findByCompanyName(String companyMail);


}
