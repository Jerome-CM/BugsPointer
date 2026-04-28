package com.bugspointer.repository;

import com.bugspointer.entity.AdminBilling;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AdminBillingRepository extends CrudRepository<AdminBilling, Long> {

    List<AdminBilling> findAllByOrderByBillingDateDesc();
}
