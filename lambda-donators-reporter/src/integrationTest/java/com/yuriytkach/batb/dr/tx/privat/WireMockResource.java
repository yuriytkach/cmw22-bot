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

package com.yuriytkach.batb.dr.tx.privat;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;

import com.github.tomakehurst.wiremock.WireMockServer;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
public class WireMockResource implements QuarkusTestResourceLifecycleManager {

  public static final WireMockServer WIREMOCK;

  static {
    try (ServerSocket socket = new ServerSocket(0)) {
      WIREMOCK = new WireMockServer(socket.getLocalPort());
    } catch (final IOException ex) {
      throw new IllegalArgumentException(ex);
    }
  }

  @Getter
  private static Integer mappedPort;
  @Getter
  private static String containerHost;

  @Override
  public Map<String, String> start() {
    WIREMOCK.start();

    containerHost = "localhost";
    mappedPort = WIREMOCK.port();

    final String url = "http://" + containerHost + ":" + mappedPort;
    log.info("WireMock URL: {}", url);

    return Map.of(
      "quarkus.rest-client.privatbank.url", url
    );
  }

  @Override
  public void stop() {
    WIREMOCK.stop();
  }
}
