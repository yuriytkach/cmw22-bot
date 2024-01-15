package com.yuriytkach.batb.bsu.bank.mono.api;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.yuriytkach.batb.common.util.RestClientLoggingProvider;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/bank")
@Produces(MediaType.APPLICATION_JSON)
@Consumes("application/json;charset=utf8")
@RegisterRestClient(configKey = "monobank")
@RegisterProvider(RestClientLoggingProvider.class)
@ApplicationScoped
public interface MonoApi {

  @GET
  @Path("/jar/{id}")
  MonoJar jarStatus(
    @PathParam("id") String id
  );

}
