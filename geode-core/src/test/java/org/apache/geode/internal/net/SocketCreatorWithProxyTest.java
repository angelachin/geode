package org.apache.geode.internal.net;

import static java.net.Proxy.Type.SOCKS;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

import org.junit.Test;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;

public class SocketCreatorWithProxyTest {
  @Test
  public void connectWithProxy() throws IOException {
    final Proxy proxy =
        new Proxy(SOCKS, InetSocketAddress.createUnresolved("104.198.221.247", 10800));

    Socket socket = new Socket(proxy);
    socket.connect(InetSocketAddress.createUnresolved("cluster-sample-locator", 10334));
    assertThat(socket.isConnected()).isTrue();
    socket.close();
    assertThat(socket.isClosed()).isTrue();
  }

  @Test
  public void connectToLocatorWithProxy() {
    final ClientCache client = new ClientCacheFactory()
        .addPoolLocator("cluster-sample-locator", 10334)
        .create();

    final Region region = client.createClientRegionFactory(ClientRegionShortcut.PROXY)
        .create("K8sBenchmark");

    assertThat(region.sizeOnServer()).isEqualTo(1000000);
  }
}
