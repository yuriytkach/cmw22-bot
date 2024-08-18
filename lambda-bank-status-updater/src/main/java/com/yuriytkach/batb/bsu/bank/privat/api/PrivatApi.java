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

package com.yuriytkach.batb.bsu.bank.privat.api;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.yuriytkach.batb.common.util.RestClientLoggingProvider;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/api/statements")
@Produces(MediaType.APPLICATION_JSON)
@Consumes("application/json;charset=utf8")
@ClientHeaderParam(name = "User-Agent", value = "tracker")
@ClientHeaderParam(name = "Content-Type", value = "application/json;charset=utf8")
@RegisterRestClient(configKey = "privatbank")
@RegisterProvider(RestClientLoggingProvider.class)
@ApplicationScoped
public interface PrivatApi {

  @GET
  @Path("/balance")
  BalancesResponse finalBalances(
    @HeaderParam("token") String token,
    @QueryParam("startDate") String startDate,
    @QueryParam("followId") String followId
  );
}
