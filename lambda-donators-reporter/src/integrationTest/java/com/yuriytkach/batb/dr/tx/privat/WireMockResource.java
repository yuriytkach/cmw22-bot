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
