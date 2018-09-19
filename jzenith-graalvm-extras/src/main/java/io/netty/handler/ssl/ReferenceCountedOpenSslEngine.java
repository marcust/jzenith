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

import io.netty.util.ReferenceCounted;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import java.nio.ByteBuffer;

public class ReferenceCountedOpenSslEngine extends SSLEngine implements ReferenceCounted, ApplicationProtocolAccessor {

    @Override
    public String getNegotiatedApplicationProtocol() {
        // empty dummy implementation
        return null;
    }

    @Override
    public int refCnt() {
        // empty dummy implementation
        return 0;
    }

    @Override
    public ReferenceCounted retain() {
        // empty dummy implementation
        return null;
    }

    @Override
    public ReferenceCounted retain(int increment) {
        // empty dummy implementation
        return null;
    }

    @Override
    public ReferenceCounted touch() {
        // empty dummy implementation
        return null;
    }

    @Override
    public ReferenceCounted touch(Object hint) {
        // empty dummy implementation
        return null;
    }

    @Override
    public boolean release() {
        // empty dummy implementation
        return false;
    }

    @Override
    public boolean release(int decrement) {
        // empty dummy implementation
        return false;
    }

    @Override
    public void beginHandshake() throws SSLException {
        // empty dummy implementation

    }

    @Override
    public void closeInbound() throws SSLException {
        // empty dummy implementation

    }

    @Override
    public void closeOutbound() {
        // empty dummy implementation

    }

    @Override
    public Runnable getDelegatedTask() {
        // empty dummy implementation
        return null;
    }

    @Override
    public boolean getEnableSessionCreation() {
        // empty dummy implementation
        return false;
    }

    @Override
    public String[] getEnabledCipherSuites() {
        // empty dummy implementation
        return null;
    }

    @Override
    public String[] getEnabledProtocols() {
        // empty dummy implementation
        return null;
    }

    @Override
    public HandshakeStatus getHandshakeStatus() {
        // empty dummy implementation
        return null;
    }

    @Override
    public boolean getNeedClientAuth() {
        // empty dummy implementation
        return false;
    }

    @Override
    public SSLSession getSession() {
        // empty dummy implementation
        return null;
    }

    @Override
    public String[] getSupportedCipherSuites() {
        // empty dummy implementation
        return null;
    }

    @Override
    public String[] getSupportedProtocols() {
        // empty dummy implementation
        return null;
    }

    @Override
    public boolean getUseClientMode() {
        // empty dummy implementation
        return false;
    }

    @Override
    public boolean getWantClientAuth() {
        // empty dummy implementation
        return false;
    }

    @Override
    public boolean isInboundDone() {
        // empty dummy implementation
        return false;
    }

    @Override
    public boolean isOutboundDone() {
        // empty dummy implementation
        return false;
    }

    @Override
    public void setEnableSessionCreation(boolean arg0) {
        // empty dummy implementation

    }

    @Override
    public void setEnabledCipherSuites(String[] arg0) {
        // empty dummy implementation

    }

    @Override
    public void setEnabledProtocols(String[] arg0) {
        // empty dummy implementation

    }

    @Override
    public void setNeedClientAuth(boolean arg0) {
        // empty dummy implementation

    }

    @Override
    public void setUseClientMode(boolean arg0) {
        // empty dummy implementation

    }

    @Override
    public void setWantClientAuth(boolean arg0) {
        // empty dummy implementation

    }

    @Override
    public SSLEngineResult unwrap(ByteBuffer arg0, ByteBuffer[] arg1, int arg2, int arg3) throws SSLException {
        // empty dummy implementation
        return null;
    }

    @Override
    public SSLEngineResult wrap(ByteBuffer[] arg0, int arg1, int arg2, ByteBuffer arg3) throws SSLException {
        // empty dummy implementation
        return null;
    }

}
