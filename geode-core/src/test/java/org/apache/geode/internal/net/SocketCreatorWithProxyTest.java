package org.apache.geode.internal.net;

import static java.net.Proxy.Type.SOCKS;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketException;

import org.junit.Test;

import org.apache.geode.internal.net.proxy.SniProxySocket;

public class SocketCreatorWithProxyTest {
  @Test
  public void connectWithProxy() throws IOException {
    final Proxy proxy =
        new Proxy(SOCKS, InetSocketAddress.createUnresolved("104.198.221.247", 10800));

    Socket socket = new Socket(proxy);
    socket.connect(InetSocketAddress.createUnresolved("cluster-sample-locator", 10334));
    assertThat(socket.isConnected()).isTrue();
    assertThat(socket.getRemoteSocketAddress())
        .isEqualTo(new InetSocketAddress((InetAddress) null, 10334));
    socket.close();
    assertThat(socket.isClosed()).isTrue();
  }

  @Test
  public void connectToLocatorWithProxy() throws SocketException {
    SniProxySocket sniProxySocket = new SniProxySocket(new InetSocketAddress("localhost", 1234));
  }

}
