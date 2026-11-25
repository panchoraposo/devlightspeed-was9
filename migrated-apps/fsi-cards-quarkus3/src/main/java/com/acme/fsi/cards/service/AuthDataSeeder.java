package com.acme.fsi.cards.service;

import com.acme.fsi.cards.model.AuthEntity;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AuthDataSeeder {

  @Inject
  AuthLedgerService ledger;

  @Transactional
  void onStart(@Observes StartupEvent ev) {
    List<AuthEntity> existing = ledger.listLatest(1);
    if (existing != null && !existing.isEmpty()) {
      return;
    }

    for (int i = 0; i < 5; i++) {
      AuthEntity e = new AuthEntity();
      e.setMerchantId("MERCH-" + (1000 + i));
      e.setPanLast4(String.format("%04d", 1000 + i));
      e.setAmount(new BigDecimal("20.00").add(new BigDecimal(i * 5)));
      e.setCurrency(i % 2 == 0 ? "USD" : "EUR");
      e.setFlaggedFlag(Boolean.FALSE);
      ledger.record("seed-" + UUID.randomUUID(), e);
    }
  }
}
