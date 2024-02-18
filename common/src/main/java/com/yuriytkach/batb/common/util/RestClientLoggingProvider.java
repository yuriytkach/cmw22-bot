package com.yuriytkach.batb.common.util;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.utils.IoUtils;

@Slf4j
public class RestClientLoggingProvider implements ClientResponseFilter {

  @Override
  public void filter(final ClientRequestContext requestContext, final ClientResponseContext responseContext) {
    final Response.StatusType statusInfo = responseContext.getStatusInfo();

    log.info(
      "Response [{} {}] - {} {}://{}{}{}{}",
      statusInfo.getStatusCode(),
      statusInfo.toEnum(),
      requestContext.getMethod(),
      requestContext.getUri().getScheme(),
      requestContext.getUri().getHost(),
      requestContext.getUri().getPort() > -1 ? ":" + requestContext.getUri().getPort() : "",
      requestContext.getUri().getPath(),
      requestContext.getUri().getQuery() == null ? "" : "?" + requestContext.getUri().getQuery()
    );

    if (statusInfo.getFamily() != Response.Status.Family.SUCCESSFUL) {
      if (responseContext.hasEntity()) {
        try {
          final String resp = IoUtils.toUtf8String(responseContext.getEntityStream());
          log.info("Response Entity: {}", resp);
        } catch (final Exception ex) {
          log.warn("Can't read response entity: {}", ex.getMessage());
        }
      }
    }
  }

}
