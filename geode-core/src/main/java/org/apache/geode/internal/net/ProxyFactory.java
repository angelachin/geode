package org.apache.geode.internal.net;

import static java.net.InetSocketAddress.createUnresolved;
import static java.net.Proxy.NO_PROXY;
import static java.net.Proxy.Type.SOCKS;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.geode.logging.internal.log4j.api.LogService.getLogger;

import java.net.Proxy;
import java.net.URI;

import org.apache.logging.log4j.Logger;


public class ProxyFactory {
  private static final String SOCKS5 = "socks5";
  private static final int DEFAULT_SOCKS5_PORT = 10800;

  private static final Logger logger = getLogger();

  static Proxy create(final String uriString) {
    if (isBlank(uriString)) {
      return NO_PROXY;
    }

    final URI uri;
    try {
      uri = URI.create(uriString);
    } catch (IllegalArgumentException e) {
      logger.fatal("Improperly configured proxy. {}", uriString);
      throw new IllegalStateException("Impropertly configured proxy.", e);
    }

    final String scheme = uri.getScheme();
    if (!(null == scheme || SOCKS5.equals(scheme))) {
      throw new IllegalArgumentException("Unsupported proxy type " + scheme + ".");
    }
    final String host = uri.getHost();
    final int port = -1 == uri.getPort() ? DEFAULT_SOCKS5_PORT : uri.getPort();
    return new Proxy(SOCKS, createUnresolved(host, port));
  }
}
