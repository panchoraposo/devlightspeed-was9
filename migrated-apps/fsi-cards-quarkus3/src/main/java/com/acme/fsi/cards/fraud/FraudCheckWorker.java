package com.acme.fsi.cards.fraud;

import com.acme.fsi.cards.service.AuthStatusStore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * In-process replacement for legacy JMS FraudQueueProducer + FraudProcessorMDB.
 * Submits fraud checks to a small worker pool so authorize() still returns
 * ACCEPTED / ENQUEUED immediately, then updates AuthStatusStore to CLEARED/FLAGGED.
 */
@ApplicationScoped
public class FraudCheckWorker {

  private final ExecutorService executor = Executors.newFixedThreadPool(2);

  @Inject
  AuthStatusStore store;

  @Inject
  ObjectMapper objectMapper;

  public void submit(String payload) {
    executor.execute(() -> process(payload));
  }

  void process(String payload) {
    try {
      JsonNode obj = objectMapper.readTree(payload == null ? "{}" : payload);
      String authId = obj.path("authId").asText("");

      // Same legacy "signature" rule: MD5 + base64 of the payload
      String signature = Base64.getEncoder().encodeToString(md5(payload));
      if (signature.length() > 0) {
        store.setStatus(authId, "CLEARED");
      } else {
        store.setStatus(authId, "FLAGGED");
      }
    } catch (Exception e) {
      // legacy: swallow exception
    }
  }

  private byte[] md5(String s) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update((s == null ? "" : s).getBytes(StandardCharsets.UTF_8));
      return md.digest();
    } catch (Exception e) {
      return new byte[0];
    }
  }

  @PreDestroy
  void shutdown() {
    executor.shutdownNow();
  }
}
