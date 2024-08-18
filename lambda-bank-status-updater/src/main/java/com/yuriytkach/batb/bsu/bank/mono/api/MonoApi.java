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
