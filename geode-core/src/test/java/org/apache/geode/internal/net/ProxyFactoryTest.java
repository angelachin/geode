package org.apache.geode.internal.net;

import static java.net.InetSocketAddress.createUnresolved;
import static java.net.Proxy.NO_PROXY;
import static org.apache.geode.internal.net.ProxyFactory.create;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.Proxy;

import org.junit.Test;

public class ProxyFactoryTest {
  @Test
  public void createNoProxyWhenNullOrEmpty() {
    assertThat(create(null)).isEqualTo(NO_PROXY);
    assertThat(create("")).isEqualTo(NO_PROXY);
    assertThat(create(" ")).isEqualTo(NO_PROXY);
  }

  @Test
  public void createThrowsExceptionWhenNotDefaultOrSocks5() {
    assertThatThrownBy(() -> {
      create("socks4://proxy:123");
    }).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> {
      create("http://proxy:123");
    }).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> {
      create("fake://proxy:123");
    }).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void createParsesSupportedValues() {
    final Proxy defaultProxy = new Proxy(Proxy.Type.SOCKS, createUnresolved("proxy", 10800));
    final Proxy altProxy = new Proxy(Proxy.Type.SOCKS, createUnresolved("proxy", 10801));
    // TODO assertThat(SocketCreator.create("proxy")).isEqualTo(defaultProxy);
    // TODO assertThat(SocketCreator.create("proxy:10081")).isEqualTo(altProxy);
    assertThat(create("socks5://proxy")).isEqualTo(defaultProxy);
    assertThat(create("socks5://proxy:10801")).isEqualTo(altProxy);
  }

}
