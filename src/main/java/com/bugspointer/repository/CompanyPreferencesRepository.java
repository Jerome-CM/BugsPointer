package com.bugspointer.repository;

import com.bugspointer.entity.Company;
import com.bugspointer.entity.CompanyPreferences;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyPreferencesRepository extends CrudRepository<CompanyPreferences, Long> {

    Optional<CompanyPreferences> findByCompany(Company company);

    Optional<CompanyPreferences> findByCompany_PublicKey(String publicKey);
}
