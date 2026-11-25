package com.acme.fsi.kyc.service;

import com.acme.fsi.kyc.model.KycApplication;
import com.acme.fsi.kyc.model.KycDecision;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

/** Business logic migrated from legacy {@code KycServiceImpl}. */
@ApplicationScoped
public class KycDecisionService {

  @Inject
  KycCaseStore store;

  @Inject
  KycConfig config;

  @Inject
  KycAuditHasher auditHasher;

  public KycDecision submitApplication(KycApplication application) {
    String scoringMode = config.scoringMode();

    String applicationId = application != null && application.getApplicationId() != null
        && !application.getApplicationId().trim().isEmpty()
        ? application.getApplicationId().trim()
        : "KYC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    if (application == null) {
      application = new KycApplication();
    }
    application.setApplicationId(applicationId);

    String status = "APPROVED";
    String reason = "auto-" + scoringMode;
    if (application.getNationalId() == null || application.getNationalId().trim().isEmpty()) {
      status = "REJECTED";
      reason = "missing-national-id";
    } else if (application.getFullName() == null || application.getFullName().trim().length() < 3) {
      status = "REVIEW";
      reason = "weak-identity";
    }

    KycDecision decision = new KycDecision();
    decision.setApplicationId(applicationId);
    decision.setStatus(status);
    decision.setReason(reason);
    decision.setAuditToken(auditHasher.token(applicationId + ":" + status));
    store.put(decision);
    return decision;
  }

  public KycDecision getStatus(String applicationId) {
    KycDecision existing = store.get(applicationId);
    if (existing != null) {
      return existing;
    }
    KycDecision missing = new KycDecision();
    missing.setApplicationId(applicationId);
    missing.setStatus("NOT_FOUND");
    missing.setReason("unknown");
    return missing;
  }
}
