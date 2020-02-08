package org.apache.geode.internal.net.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;


public class SniProxySocket extends Socket {
  private final InetSocketAddress proxyAddress;
  private InetSocketAddress endpoint;

  public SniProxySocket(final InetSocketAddress proxyAddress) throws SocketException {
    super();

    this.proxyAddress = proxyAddress;
  }

  @Override
  public void connect(SocketAddress endpoint, int timeout) throws IOException {
    super.connect(proxyAddress, timeout);
    this.endpoint = (InetSocketAddress) endpoint;
  }

  @Override
  public SocketAddress getRemoteSocketAddress() {
    return new InetSocketAddress(endpoint.getHostName(), endpoint.getPort());
  }
}
