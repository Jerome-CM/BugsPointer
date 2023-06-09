package com.bugspointer.repository;

import com.bugspointer.entity.Company;
import com.bugspointer.entity.Customer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, Long> {

    Optional<Customer> findByCompany_CompanyName(String companyMail);

    Optional<Customer> findByCustomerId(String customerId);

    Optional<Customer> findByCompany_CompanyId(Long companyId);
}
