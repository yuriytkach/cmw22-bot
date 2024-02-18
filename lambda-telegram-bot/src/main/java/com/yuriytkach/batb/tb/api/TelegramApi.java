package com.yuriytkach.batb.tb.api;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.yuriytkach.batb.common.util.RestClientLoggingProvider;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/bot{token}")
@RegisterRestClient(configKey = "telegram")
@RegisterProvider(RestClientLoggingProvider.class)
@ApplicationScoped
public interface TelegramApi {

  @POST
  @Path("/{operation}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  Response send(
    @PathParam("token") String token,
    @PathParam("operation") String operation,
    Object body
  );
}
