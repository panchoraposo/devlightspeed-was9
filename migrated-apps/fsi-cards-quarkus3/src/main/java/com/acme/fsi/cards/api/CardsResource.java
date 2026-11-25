package com.acme.fsi.cards.api;

import com.acme.fsi.cards.fraud.FraudCheckWorker;
import com.acme.fsi.cards.model.AuthEntity;
import com.acme.fsi.cards.service.AuthLedgerService;
import com.acme.fsi.cards.service.AuthStatusStore;
import com.acme.fsi.cards.service.LegacyJndiCardsLookup;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/auths")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CardsResource {

  @Inject
  FraudCheckWorker fraudWorker;

  @Inject
  AuthStatusStore store;

  @Inject
  AuthLedgerService ledger;

  @Inject
  LegacyJndiCardsLookup dsLookup;

  @GET
  public Response list(@QueryParam("limit") Integer limit) {
    int l = (limit == null) ? 10 : limit.intValue();
    List<AuthEntity> rows = ledger.listLatest(l);
    List<String> json = new ArrayList<String>(rows.size());
    for (AuthEntity e : rows) {
      json.add("{"
          + "\"authId\":\"" + e.getAuthId() + "\","
          + "\"merchantId\":\"" + e.getMerchantId() + "\","
          + "\"amount\":\"" + e.getAmount() + "\","
          + "\"currency\":\"" + e.getCurrency() + "\","
          + "\"flaggedFlag\":\"" + (Boolean.TRUE.equals(e.getFlaggedFlag()) ? "Y" : "N") + "\""
          + "}");
    }
    return Response.ok("[" + String.join(",", json) + "]").type(MediaType.APPLICATION_JSON).build();
  }

  @POST
  public Response authorize(AuthRequest req) {
    if (req == null || isBlank(req.getPan()) || isBlank(req.getMerchantId())) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("{\"error\":\"pan and merchantId are required\"}")
          .type(MediaType.APPLICATION_JSON)
          .build();
    }
    BigDecimal amount = req.getAmount() == null ? new BigDecimal("25.00") : req.getAmount();
    String currency = isBlank(req.getCurrency()) ? "USD" : req.getCurrency().trim();
    String authId = UUID.randomUUID().toString();
    String panLast4 = req.getPan().length() >= 4
        ? req.getPan().substring(req.getPan().length() - 4)
        : req.getPan();

    String payload = "{\"authId\":\"" + authId + "\",\"amount\":\"" + amount
        + "\",\"currency\":\"" + currency + "\",\"merchantId\":\"" + req.getMerchantId() + "\"}";
    store.setStatus(authId, "ENQUEUED");
    fraudWorker.submit(payload);

    AuthEntity e = new AuthEntity();
    e.setMerchantId(req.getMerchantId().trim());
    e.setPanLast4(panLast4);
    e.setAmount(amount);
    e.setCurrency(currency);
    e.setFlaggedFlag(Boolean.FALSE);
    ledger.record(authId, e);

    // Name kept for story continuity; now a CDI DataSource inject shim
    dsLookup.lookupCardsDs();

    return Response.status(Response.Status.ACCEPTED)
        .entity("{\"authId\":\"" + authId + "\",\"status\":\"ENQUEUED\"}")
        .type(MediaType.APPLICATION_JSON)
        .build();
  }

  @GET
  @Path("/{authId}")
  public Response status(@PathParam("authId") String authId) {
    String status = store.getStatus(authId);
    if (status == null) {
      List<AuthEntity> rows = ledger.findByAuthId(authId);
      if (rows != null && !rows.isEmpty()) {
        AuthEntity e = rows.get(0);
        String derived = Boolean.TRUE.equals(e.getFlaggedFlag()) ? "FLAGGED" : "RECORDED";
        return Response.ok("{\"authId\":\"" + authId + "\",\"status\":\"" + derived + "\"}")
            .type(MediaType.APPLICATION_JSON)
            .build();
      }
      return Response.status(Response.Status.NOT_FOUND)
          .entity("{\"authId\":\"" + authId + "\",\"status\":\"NOT_FOUND\"}")
          .type(MediaType.APPLICATION_JSON)
          .build();
    }
    return Response.ok("{\"authId\":\"" + authId + "\",\"status\":\"" + status + "\"}")
        .type(MediaType.APPLICATION_JSON)
        .build();
  }

  private boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }
}
