package com.yuriytkach.batb.tb.api;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.yuriytkach.batb.common.util.RestClientLoggingProvider;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/bot{token}")
@RegisterRestClient(configKey = "telegram")
@RegisterProvider(RestClientLoggingProvider.class)
@ApplicationScoped
public interface TelegramApi {

  @GET
  @Path("/sendMessage")
  @Produces(MediaType.APPLICATION_JSON)
  Response sendMessage(
    @PathParam("token") String token,
    @QueryParam("chat_id") String chatId,
    @QueryParam("text") String text
  );
}
