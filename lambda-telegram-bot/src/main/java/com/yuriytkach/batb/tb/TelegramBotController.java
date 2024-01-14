package com.yuriytkach.batb.tb;

import com.yuriytkach.batb.common.BankAccountStatus;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/privat")
@RequiredArgsConstructor
public class TelegramBotController {

  @POST
  @Path("/event")
  public Response hook() {
    log.info("Telegram Bot Hook: {}", BankAccountStatus.builder().build());
    return Response.ok().build();
  }

}
