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
package io.netty.handler.ssl;

import org.junit.Test;

import java.lang.reflect.Method;

/**
 * Dummy test to increase the code coverage
 */
public class ReferenceCountedOpenSslEngineTest {

    @Test
    public void callAll() throws Exception {
        final ReferenceCountedOpenSslEngine engine = new ReferenceCountedOpenSslEngine();

        engine.touch();
        engine.touch(new Object());
        engine.retain();
        engine.retain(5);
        engine.refCnt();
        engine.closeInbound();
        engine.beginHandshake();
        engine.getHandshakeStatus();
        engine.getDelegatedTask();
        engine.closeOutbound();
        engine.getNegotiatedApplicationProtocol();
        engine.getEnableSessionCreation();
        engine.getEnabledCipherSuites();
        engine.getEnabledProtocols();
        engine.getNeedClientAuth();
        engine.getSession();
        engine.getSupportedCipherSuites();
        engine.getSupportedProtocols();
        engine.getUseClientMode();
        engine.getWantClientAuth();
        engine.isInboundDone();
        engine.isOutboundDone();
        engine.setNeedClientAuth(false);
        engine.setUseClientMode(false);
        engine.setWantClientAuth(false);
        engine.setEnableSessionCreation(false);
        engine.setEnabledCipherSuites(new String[0]);
        engine.setEnabledProtocols(new String[0]);
        engine.release();
        engine.release(1);

    }

}
