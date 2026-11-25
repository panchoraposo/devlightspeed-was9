package com.acme.fsi.kyc.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Replaces legacy {@code LegacyKycConfig}: hardcoded {@code /etc/fsi/kyc/scoring.properties}
 * and {@code -Dfsi.kyc.scoring.mode} system property → MicroProfile Config.
 */
@ApplicationScoped
public class KycConfig {

  @ConfigProperty(name = "fsi.kyc.scoring.mode", defaultValue = "basic")
  String scoringMode;

  public String scoringMode() {
    return scoringMode == null || scoringMode.isBlank() ? "basic" : scoringMode.trim();
  }
}
