package com.bugspointer.repository;

import com.bugspointer.entity.CompanyToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyTokenRepository extends CrudRepository<CompanyToken, Long> {

    Optional<CompanyToken> findByPublicKey(String publicKey);
}
