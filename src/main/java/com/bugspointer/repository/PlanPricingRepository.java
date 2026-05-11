package com.bugspointer.repository;

import com.bugspointer.entity.EnumPlan;
import com.bugspointer.entity.PlanPricing;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface PlanPricingRepository extends CrudRepository<PlanPricing, Long> {

    Optional<PlanPricing> findByPlan(EnumPlan plan);

    List<PlanPricing> findAllByOrderByPlanAsc();
}
