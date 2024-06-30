package com.yuriytkach.batb.dr.stats;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.yuriytkach.batb.common.telega.TelegramBotSendMessageRequest;
import com.yuriytkach.batb.common.util.RestClientLoggingProvider;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/bot")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "telegram-bot")
@RegisterProvider(RestClientLoggingProvider.class)
@ApplicationScoped
public interface TelegramBotApi {

  @POST
  @Path("/telegram")
  void sendMessage(
    @HeaderParam("X-Telegram-Bot-Api-Secret-Token") String secretHeader,
    TelegramBotSendMessageRequest body
  );
}
