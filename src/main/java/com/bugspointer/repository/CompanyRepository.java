package com.bugspointer.repository;

import com.bugspointer.entity.Company;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends CrudRepository<Company, Long> {

    Optional<Company> findByMail(String mail);

    Optional<Company> findByCompanyName(String companyMail);

    /*@Query(value="select * from company where public_key=:key",nativeQuery = true)
    Optional<Company> findByPublicKey(@Param("key") String publicKey);*/

    Optional<Company> findByPublicKey(String publicKey);
}
