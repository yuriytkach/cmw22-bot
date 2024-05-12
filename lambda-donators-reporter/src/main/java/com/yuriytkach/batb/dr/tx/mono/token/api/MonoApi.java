package com.yuriytkach.batb.dr.tx.mono.token.api;

import java.time.temporal.ChronoUnit;
import java.util.List;

import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.yuriytkach.batb.common.util.RestClientLoggingProvider;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/personal")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "monobank")
@RegisterProvider(RestClientLoggingProvider.class)
@RegisterProvider(MonoApi.ApiExceptionMapper.class)
@ApplicationScoped
public interface MonoApi {

  @GET
  @Path("/client-info")
  Client clientInfo(@HeaderParam("x-token") String token);

  @GET
  @Path("/statement/{account}/{from}/{to}")
  @Retry(delay = 65, delayUnit = ChronoUnit.SECONDS, retryOn = MyTooManyRequestsException.class)
  List<MonoStatement> statements(
    @HeaderParam("x-token") String token,
    @PathParam("account") String account,
    @PathParam("from") long from,
    @PathParam("to") long to
  );

  class ApiExceptionMapper implements ResponseExceptionMapper<WebApplicationException> {

    @Override
    public WebApplicationException toThrowable(final Response response) {
      if (response.getStatus() == 429) {
        return new MyTooManyRequestsException("Too many requests");
      }
      return new WebApplicationException(response);
    }
  }

  class MyTooManyRequestsException extends WebApplicationException {
    public MyTooManyRequestsException(final String message) {
      super(message, 429);
    }
  }

}
