//  GParallelizer
//
//  Copyright © 2008-9  The original author or authors
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.gparallelizer.remote.netty;

import org.jboss.netty.channel.*;
import org.gparallelizer.remote.RemoteConnection;
import org.gparallelizer.remote.netty.RemoteObjectEncoder;
import org.gparallelizer.remote.netty.RemoteObjectDecoder;
import org.gparallelizer.remote.messages.BaseMsg;

/**
 * @author Alex Tkachman
 */
@ChannelPipelineCoverage("one")
public class NettyHandler extends SimpleChannelHandler {

    private Channel channel;

    private final RemoteConnection connection;

    public NettyHandler(NettyTransportProvider provider) {
        connection = new NettyRemoteConnection(provider, this);
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        channel = e.getChannel();
        channel.getPipeline().addFirst("encoder", new RemoteObjectEncoder(connection));
        channel.getPipeline().addFirst("decoder", new RemoteObjectDecoder(connection));
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        connection.onConnect();
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        connection.onDisconnect();
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        connection.onMessage((BaseMsg) e.getMessage());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        //noinspection ThrowableResultOfMethodCallIgnored
        connection.onException(e.getCause());
        e.getCause().printStackTrace();
    }

    public Channel getChannel() {
        return channel;
    }
}
