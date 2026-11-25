package com.acme.fsi.kyc.service;

import com.acme.fsi.kyc.model.KycDecision;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory store (no JPA) — same shape as the legacy SOAP demo. */
@ApplicationScoped
public class KycCaseStore {

  private final Map<String, KycDecision> cases = new ConcurrentHashMap<>();

  public void put(KycDecision decision) {
    if (decision == null || decision.getApplicationId() == null) {
      return;
    }
    cases.put(decision.getApplicationId(), decision);
  }

  public KycDecision get(String applicationId) {
    if (applicationId == null) {
      return null;
    }
    return cases.get(applicationId);
  }
}
