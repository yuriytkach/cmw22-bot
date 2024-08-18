/*
 * Copyright (c) 2024  Commonwealth-22
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
