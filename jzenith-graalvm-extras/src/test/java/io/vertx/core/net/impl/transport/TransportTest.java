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
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.SocketAddress;
import org.junit.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Dummy test to increase the code coverage
 */
public class TransportTest {

    @Test
    public void testVoidTest() {
        final Transport transport = Transport.JDK;

        assertThat(transport.isAvailable()).isTrue();
        assertThat(transport.channelOption("foo")).isNull();
        assertThat(transport.channelType(false)).isEqualTo(NioSocketChannel.class);
        assertThat(transport.serverChannelType(false)).isEqualTo(NioServerSocketChannel.class);
        assertThat(transport.datagramChannel()).isInstanceOf(NioDatagramChannel.class);

        transport.configure(new NioDatagramChannel(), new DatagramSocketOptions());
        transport.configure(new NetServerOptions(), new ServerBootstrap());
        transport.configure(new HttpClientOptions(), new Bootstrap());
        final SocketAddress address = mock(SocketAddress.class);
        when(address.host()).thenReturn("localhost");
        transport.convert(address, true);
        transport.convert(address, false);

        transport.eventLoopGroup(5, Thread::new, 5);
        transport.datagramChannel(InternetProtocolFamily.IPv4);
        transport.datagramChannel(InternetProtocolFamily.IPv6);

        assertThat(transport.unavailabilityCause()).isNull();
    }

}
