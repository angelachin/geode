package org.apache.geode.internal.net.proxy;

import static org.apache.geode.test.awaitility.GeodeAwaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class SniProxySocketTest {

  // private ServerSocket proxyServerSocket;
  private SocketChannel proxySocketChannel;
  private Thread proxyServerThread;
  // private volatile Socket proxySocket;
  private volatile ServerSocketChannel proxyServerChannel;
  private ServerSocket internalServerSocket;
  private Thread internalServerThread;
  private volatile Socket internalSocket;


  @Before
  public void before() throws IOException {
//    final Selector proxySelector = Selector.open();
//    proxyServerChannel = ServerSocketChannel.open();
//    proxyServerChannel.configureBlocking(false);
//    proxyServerChannel.bind(new InetSocketAddress(0));
//    proxyServerChannel.register(proxySelector, SelectionKey.OP_ACCEPT);
//    proxyServerThread = new Thread(() -> {
//      try {
//        while (!Thread.currentThread().isInterrupted()) {
//          if (proxySelector.select() <= 0) {
//            continue;
//          }
//
//          final Set<SelectionKey> selectedKeys = proxySelector.selectedKeys();
//          final Iterator<SelectionKey> iterator = selectedKeys.iterator();
//          while (iterator.hasNext()) {
//            final SelectionKey key = (SelectionKey) iterator.next();
//            iterator.remove();
//            if (key.isAcceptable()) {
//              proxySocketChannel = proxyServerChannel.accept();
//              proxySocketChannel.configureBlocking(false);
//              proxySocketChannel.register(proxySelector, SelectionKey.OP_READ);
//            }
//            if (key.isReadable()) {
//              final String hostname;
//              final SocketChannel sc = (SocketChannel) key.channel();
//              final ByteBuffer bb = ByteBuffer.allocate(1024);
//              sc.read(bb);
//              bb.get();
//              bb.getShort();
//              bb.getShort();
//              bb.get();
//              bb.position(bb.position() + 3);
//              bb.getShort();
//              bb.position(bb.position() + 32);
//              bb.get();
//              int skip = bb.getShort();
//              bb.position(bb.position() + skip);
//              bb.get();
//              bb.get();
//              bb.getShort(); // extensions length
//              while (true) {
//                int type = bb.get();
//                int len = bb.getShort();
//                if (type != 0) {
//                  bb.position(bb.position() + len);
//                }
//                bb.getShort();
//                bb.get();
//                len = bb.getShort();
//                hostname = new String(bb.array(), bb.arrayOffset(), len, Charset.forName("ASCII"));
//                break;
//              }
//
//              SocketChannel endpointSocketChannel = SocketChannel
//                  .open(new InetSocketAddress(hostname, internalServerSocket.getLocalPort()));
//              endpointSocketChannel.configureBlocking(false);
//              // TODO need different selector endpointSocketChannel.register(proxySelector,
//              // SelectionKey.OP_READ);
//            }
//          }
//        }
//      } catch (Exception e) {
//        e.printStackTrace();
//      }
//    });


    // proxyServerSocket = new ServerSocket(0);
    // proxyServerThread = new Thread(() -> {
    // try {
    // proxySocket = proxyServerSocket.accept();
    // assertThat(proxySocket.isConnected()).isTrue();
    //
    // final ByteBuffer inBytes = ByteBuffer.allocate(10000)
    // final SocketChannel proxyChannel = proxySocket.getChannel();
    // while (proxySocket.isConnected()) {
    // final int len = proxyChannel.read(inBytes);
    // inBytes.flip();
    // System.out.write((byte[]) inBytes.array());
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // });
//    proxyServerThread.start();
//
//    final ServerSocketFactory ssocketFactory = SSLServerSocketFactory.getDefault();
//    internalServerSocket = ssocketFactory.createServerSocket(0);
//    internalServerThread = new Thread(() -> {
//      try {
//        internalSocket = internalServerSocket.accept();
//      } catch (IOException e) {
//        // e.printStackTrace();
//      }
//    });
//    internalServerThread.start();
  }

  @After
  public void after() throws IOException, InterruptedException {
// //     proxyServerSocket.close();
//    proxyServerChannel.close();
//    close(proxyServerChannel);
//    proxyServerThread.interrupt();
//    proxyServerThread.stop();
//    proxyServerThread.join();
//
//    internalServerSocket.close();
//    close(internalSocket);
//    internalServerThread.interrupt();
//    internalServerThread.stop();
//    internalServerThread.join();
  }

  private void close(final Closeable socket) {
    if (null != socket) {
      try {
        socket.close();
      } catch (IOException ignore) {
      }
    }
  }

  @Ignore
  @Test
  public void connectInterceptsToProxy() throws IOException, NoSuchAlgorithmException {
    final SniProxySocket socket = new SniProxySocket(new InetSocketAddress("sni-proxy.lima.cf-app.com", 15443));
    assertThat(socket.isConnected()).isFalse();
//    assertThat(proxySocketChannel).isNull();
    socket.connect(InetSocketAddress.createUnresolved("dceebbfa-174b-4f71-834d-11abb9ff3063.locator.lima-services-subnet.service-instance-7b9600e8-5872-4659-8a99-9ec6c2ec8cad.bosh", 55221));
    socket.setSoTimeout(10000);
    assertThat(socket.isConnected()).isTrue();
//    await().untilAsserted(() -> {
//      assertThat(proxySocketChannel).isNotNull();
//      assertThat(proxySocketChannel.isConnected()).isTrue();
//      assertThat(internalSocket).isNull();
//    });

//    final String hostName = "localhost";
//    final SSLSocket sslSocket =
//        (SSLSocket) SSLContext.getDefault().getSocketFactory().createSocket(socket,
//            hostName, internalServerSocket.getLocalPort(),
//            false);
//    SSLParameters sslParameters = new SSLParameters();
//    sslParameters.setServerNames(Collections.singletonList(new SNIHostName(hostName)));
//    sslSocket.setSSLParameters(sslParameters);
//    sslSocket.getOutputStream().write(0);
//    sslSocket.close();
//    socket.close();
  }

  @Test
  public void getRemoteSocketAddressDoesNotDiscloseInternal() throws IOException {
    final SniProxySocket socket = new SniProxySocket(getLocalSocketAddress(proxyServerChannel));
    socket.connect(internalServerSocket.getLocalSocketAddress());
    assertThat(socket.getRemoteSocketAddress()).isEqualTo(
        new InetSocketAddress((InetAddress) null, getLocalSocketAddress(internalServerSocket)
            .getPort()));
  }

  private InetSocketAddress getLocalSocketAddress(final ServerSocket serverSocket) {
    return (InetSocketAddress) serverSocket.getLocalSocketAddress();
  }

  private InetSocketAddress getLocalSocketAddress(final ServerSocketChannel serverSocketChannel)
      throws IOException {
    return (InetSocketAddress) serverSocketChannel.getLocalAddress();
  }

}
