package com.acme.fsi.kyc.api;

import com.acme.fsi.kyc.model.KycApplication;
import com.acme.fsi.kyc.model.KycDecision;
import com.acme.fsi.kyc.service.KycDecisionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Quarkus REST replacement for JAX-WS {@code KycService} / {@code KycServiceImpl}.
 *
 * <ul>
 *   <li>{@code submitApplication} → {@code POST /api/kyc/applications}</li>
 *   <li>{@code getStatus} → {@code GET /api/kyc/applications/{applicationId}}</li>
 * </ul>
 */
@Path("/kyc")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class KycResource {

  @Inject
  KycDecisionService decisions;

  @POST
  @Path("/applications")
  public Response submitApplication(KycApplication application) {
    KycDecision decision = decisions.submitApplication(application);
    return Response.status(Response.Status.CREATED).entity(decision).build();
  }

  @GET
  @Path("/applications/{applicationId}")
  public Response getStatus(@PathParam("applicationId") String applicationId) {
    KycDecision decision = decisions.getStatus(applicationId);
    if ("NOT_FOUND".equals(decision.getStatus())) {
      return Response.status(Response.Status.NOT_FOUND).entity(decision).build();
    }
    return Response.ok(decision).build();
  }

  /** Alias matching SOAP operation name {@code getStatus}. */
  @GET
  @Path("/status/{applicationId}")
  public Response getStatusAlias(@PathParam("applicationId") String applicationId) {
    return getStatus(applicationId);
  }

  /** Alias matching SOAP operation name {@code submitApplication}. */
  @POST
  @Path("/submit")
  public Response submitAlias(KycApplication application) {
    return submitApplication(application);
  }
}
