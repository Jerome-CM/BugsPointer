package com.bugspointer.service.implementation;

import com.bugspointer.dto.EnumStatus;
import com.bugspointer.dto.Response;
import com.bugspointer.entity.EnumPlan;
import com.bugspointer.entity.PlanPricing;
import com.bugspointer.repository.PlanPricingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PlanPricingService {

    private final PlanPricingRepository planPricingRepository;

    public PlanPricingService(PlanPricingRepository planPricingRepository) {
        this.planPricingRepository = planPricingRepository;
    }

    public List<PlanPricing> getPlanPrices() {
        List<PlanPricing> prices = new ArrayList<>();
        for (EnumPlan plan : EnumPlan.values()) {
            if (plan != EnumPlan.FREE) {
                prices.add(getOrCreate(plan));
            }
        }
        return prices;
    }

    public BigDecimal getNewSubscriptionAmount(EnumPlan plan) {
        if (plan == null || plan == EnumPlan.FREE) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return normalize(getOrCreate(plan).getNewSubscriptionAmount());
    }

    public BigDecimal getRenewalAmount(EnumPlan plan) {
        if (plan == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return normalize(new BigDecimal(plan.getValeur()));
    }

    public Response updateNewSubscriptionAmount(EnumPlan plan, BigDecimal amount) {
        if (plan == null || plan == EnumPlan.FREE) {
            return new Response(EnumStatus.ERROR, null, "Plan non modifiable");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            return new Response(EnumStatus.ERROR, null, "Le prix doit être supérieur ou égal à 0");
        }

        PlanPricing pricing = getOrCreate(plan);
        pricing.setNewSubscriptionAmount(normalize(amount));
        pricing.setDateUpdate(new Date());
        planPricingRepository.save(pricing);
        return new Response(EnumStatus.OK, null, "Prix des nouvelles souscriptions mis à jour");
    }

    public boolean isRenewalAmount(BigDecimal amount, EnumPlan plan) {
        return normalize(amount).compareTo(getRenewalAmount(plan)) == 0;
    }

    public String format(BigDecimal amount) {
        return normalize(amount).toPlainString();
    }

    private PlanPricing getOrCreate(EnumPlan plan) {
        Optional<PlanPricing> existing = planPricingRepository.findByPlan(plan);
        if (existing.isPresent()) {
            return existing.get();
        }

        PlanPricing pricing = new PlanPricing();
        pricing.setPlan(plan);
        pricing.setNewSubscriptionAmount(getDefaultNewSubscriptionAmount(plan));
        return planPricingRepository.save(pricing);
    }

    private BigDecimal getDefaultNewSubscriptionAmount(EnumPlan plan) {
        if (plan == EnumPlan.TARGET) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return getRenewalAmount(plan);
    }

    private BigDecimal normalize(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
