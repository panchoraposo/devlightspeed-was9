package com.acme.fsi.kyc.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/** SHA-256 audit hashing — replaces legacy SOAP MD5 {@code KycAuditHandler}. */
@ApplicationScoped
public class KycAuditHasher {

  public String token(String input) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digest = md.digest((input == null ? "" : input).getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (Exception e) {
      return "UNSIGNED";
    }
  }
}
