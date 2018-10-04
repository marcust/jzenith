/**
 * Copyright © 2018 Marcus Thiesen (marcus@thiesen.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertx.core.net.impl.transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.SocketAddress;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;


import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Dummy test to increase the code coverage
 */
public class TransportTest {

    @Test
    public void testVoidTest() {
        final Transport transport = Transport.JDK;
        Transport.nativeTransport();

        assertThat(transport.isAvailable()).isTrue();
        assertThat(transport.channelOption("foo")).isNull();
        assertThat(transport.channelType(false)).isEqualTo(NioSocketChannel.class);
        assertThat(transport.serverChannelType(false)).isEqualTo(NioServerSocketChannel.class);
        assertThat(transport.datagramChannel()).isInstanceOf(NioDatagramChannel.class);

        final DatagramSocketOptions datagramSocketOptions = new DatagramSocketOptions();
        datagramSocketOptions.setSendBufferSize(5);
        datagramSocketOptions.setReceiveBufferSize(5);
        datagramSocketOptions.setTrafficClass(2);
        datagramSocketOptions.setMulticastTimeToLive(5);
        datagramSocketOptions.setMulticastNetworkInterface("localhost");

        try {
            transport.configure(new NioDatagramChannel(), datagramSocketOptions);
        } catch (IllegalArgumentException e) {
            // ignore
        }
        datagramSocketOptions.setMulticastNetworkInterface(null);
        transport.configure(new NioDatagramChannel(), datagramSocketOptions);

        final NetServerOptions netServerOptions = new NetServerOptions();
        netServerOptions.setSendBufferSize(5);
        netServerOptions.setReceiveBufferSize(5);
        netServerOptions.setTrafficClass(2);
        netServerOptions.setSoLinger(5);
        netServerOptions.setAcceptBacklog(5);
        transport.configure(netServerOptions, new ServerBootstrap());

        final HttpClientOptions httpClientOptions = new HttpClientOptions();
        httpClientOptions.setSendBufferSize(5);
        httpClientOptions.setReceiveBufferSize(5);
        httpClientOptions.setTrafficClass(2);
        httpClientOptions.setSoLinger(5);
        httpClientOptions.setLocalAddress("localhost");
        transport.configure(httpClientOptions, new Bootstrap());

        final SocketAddress address = mock(SocketAddress.class);
        when(address.host()).thenReturn("localhost");
        transport.convert(address, true);
        transport.convert(address, false);

        transport.eventLoopGroup(5, Thread::new, 5);
        transport.datagramChannel(InternetProtocolFamily.IPv4);

        try {
            transport.datagramChannel(InternetProtocolFamily.IPv6);
        } catch (Exception e) {
            // ignore, does not work on Travis
        }
        assertThat(transport.unavailabilityCause()).isNull();
    }

    @Test
    public void testIllegalArgumentException1() {
        assertThrows(IllegalArgumentException.class, () -> {
            final SocketAddress address = mock(SocketAddress.class);
            when(address.path()).thenReturn("/foo");

            Transport.JDK.convert(address, true);
        });
    }

    @Test
    public void testIllegalArgumentException2() {
        assertThrows(IllegalArgumentException.class, () -> Transport.JDK.channelType(true));
    }

    @Test
    public void testIllegalArgumentException3() {
        assertThrows(IllegalArgumentException.class, () -> Transport.JDK.serverChannelType(true));
    }

    @Test
    public void testChannelOptions() {
        final Transport transport = Mockito.spy(Transport.JDK);
        when(transport.channelOption(ArgumentMatchers.eq(Transport.SO_REUSEPORT))).thenReturn(ChannelOption.valueOf(Transport.SO_REUSEPORT));

        final NetServerOptions netServerOptions = new NetServerOptions();
        transport.configure(netServerOptions, new ServerBootstrap());
    }

}
