/*
 * Copyright (C) 2019~2022 dinstone<dinstone@163.com>
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
package com.dinstone.focus.client.http2;

import java.io.IOException;
import java.net.InetSocketAddress;

import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.DefaultHttp2HeadersFrame;
import io.netty.handler.codec.http2.Http2HeadersFrame;

public class Http2ChannelFactoryTest {

    public static void main(String[] args) throws IOException {

        Http2ChannelFactory factory = new Http2ChannelFactory(new Http2ConnectOptions());
        try {
            Http2Channel channel = factory.create(new InetSocketAddress("127.0.0.1", 8000));

            DefaultHttp2Headers headers = new DefaultHttp2Headers();
            headers.method("GET");
            headers.path("/");
            headers.scheme("http");
            Http2HeadersFrame headersFrame = new DefaultHttp2HeadersFrame(headers, true);
            // channel.writeAndFlush(headersFrame);

            System.out.println("ok");
        } finally {
            factory.destroy();
        }
    }

}