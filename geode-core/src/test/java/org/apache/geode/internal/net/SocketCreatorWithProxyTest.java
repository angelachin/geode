package org.apache.geode.internal.net;

import static org.apache.geode.distributed.ConfigurationProperties.SECURITY_CLIENT_AUTH_INIT;
import static org.apache.geode.distributed.ConfigurationProperties.SSL_CIPHERS;
import static org.apache.geode.distributed.ConfigurationProperties.SSL_ENABLED_COMPONENTS;
import static org.apache.geode.distributed.ConfigurationProperties.SSL_KEYSTORE;
import static org.apache.geode.distributed.ConfigurationProperties.SSL_KEYSTORE_PASSWORD;
import static org.apache.geode.distributed.ConfigurationProperties.SSL_KEYSTORE_TYPE;
import static org.apache.geode.distributed.ConfigurationProperties.SSL_PROTOCOLS;
import static org.apache.geode.distributed.ConfigurationProperties.SSL_REQUIRE_AUTHENTICATION;
import static org.apache.geode.distributed.ConfigurationProperties.SSL_TRUSTSTORE;
import static org.apache.geode.distributed.ConfigurationProperties.SSL_TRUSTSTORE_PASSWORD;
import static org.apache.geode.security.AuthInitialize.SECURITY_PASSWORD;
import static org.apache.geode.security.AuthInitialize.SECURITY_USERNAME;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;

import org.junit.Test;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.internal.net.proxy.SniProxySocket;

public class SocketCreatorWithProxyTest {
  @Test
  public void connectToLocatorWithProxy() throws IOException {
    SniProxySocket sniProxySocket = new SniProxySocket(
        new InetSocketAddress("sni-proxy.lima.cf-app.com", 15443));
    sniProxySocket.connect(InetSocketAddress.createUnresolved("cluster-sample-locator", 10334));
    assertThat(sniProxySocket.isConnected()).isTrue();
    assertThat(sniProxySocket.getRemoteSocketAddress())
        .isEqualTo(new InetSocketAddress("cluster-sample-locator", 10334));
    sniProxySocket.close();
    assertThat(sniProxySocket.isClosed()).isTrue();
  }

  @Test
  public void connectToSNIProxy() {
    Properties gemFireProps = new Properties();
    gemFireProps.setProperty(SSL_ENABLED_COMPONENTS, "all");
    gemFireProps.setProperty(SSL_KEYSTORE_TYPE, "jks");
    gemFireProps.setProperty(SECURITY_CLIENT_AUTH_INIT, "org.apache.geode.internal.net.ClientAuthInitialize");
    gemFireProps.setProperty(SSL_REQUIRE_AUTHENTICATION, "false");
    gemFireProps.setProperty(SECURITY_USERNAME, "developer_1CmrvjJLGSx2P9gVSBTpWw");
    gemFireProps.setProperty(SECURITY_PASSWORD, "MGHrSPzAYW5lYhv0WE4g");

    gemFireProps.setProperty(SSL_KEYSTORE, "/Users/pivotal/workspace/geode/keystore.jks");
    gemFireProps.setProperty(SSL_KEYSTORE_PASSWORD, "fRr7q6jwCmaVMLhLJKikb5vdGzNV91");
    gemFireProps.setProperty(SSL_TRUSTSTORE, "/Users/pivotal/workspace/geode/truststore.jks");
    gemFireProps.setProperty(SSL_TRUSTSTORE_PASSWORD, "P8Sz7ihZCf7snZb09LAHJvSWGFZdXx");

    ClientCache cache = new ClientCacheFactory(gemFireProps)
        .addPoolLocator("ffe8fc05-152d-4a4b-852b-1fd820bb68a9.locator.lima-services-subnet.service-instance-60bce0c4-e27d-445b-bcc2-226b0783e2d1.bosh", 55221)
        .create();
    Region<String, String> region = cache.<String, String>
        createClientRegionFactory(ClientRegionShortcut.CACHING_PROXY)
        .create("jellyfish");
    region.put("hello", "world");
  }
}
