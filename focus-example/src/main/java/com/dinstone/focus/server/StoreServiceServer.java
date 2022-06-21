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
package com.dinstone.focus.server;

import java.io.IOException;

import com.dinstone.focus.client.Client;
import com.dinstone.focus.client.ClientOptions;
import com.dinstone.focus.codec.protobuf.ProtobufCodec;
import com.dinstone.focus.example.StoreService;
import com.dinstone.focus.example.StoreServiceImpl;
import com.dinstone.focus.example.UserService;
import com.dinstone.focus.filter.Filter;
import com.dinstone.focus.tracing.TracingFilter;
import com.dinstone.loghub.Logger;
import com.dinstone.loghub.LoggerFactory;
import com.dinstone.photon.ConnectOptions;

import brave.Span.Kind;
import brave.Tracing;
import brave.rpc.RpcTracing;
import brave.sampler.Sampler;
import zipkin2.reporter.Sender;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;
import zipkin2.reporter.okhttp3.OkHttpSender;

public class StoreServiceServer {

    private static final Logger LOG = LoggerFactory.getLogger(StoreServiceServer.class);

    public static void main(String[] args) {

        Server sss = createStoreServiceServer();

        LOG.info("server start");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        sss.destroy();
        LOG.info("server stop");
    }

    private static Server createStoreServiceServer() {
        Sender sender = OkHttpSender.create("http://localhost:9411/api/v2/spans");
        AsyncZipkinSpanHandler spanHandler = AsyncZipkinSpanHandler.create(sender);
        Tracing tracing = Tracing.newBuilder().localServiceName("store.service").addSpanHandler(spanHandler)
                .sampler(Sampler.create(1)).build();

        final Filter tf = new TracingFilter(RpcTracing.create(tracing), Kind.SERVER);

        ServerOptions serverOptions = new ServerOptions();
        serverOptions.listen("localhost", 3302);
        serverOptions.addFilter(tf);
        Server server = new Server(serverOptions);
        UserService userService = createUserServiceRpc(tracing);
        server.exporting(StoreService.class, new StoreServiceImpl(userService));

        return server;
    }

    private static UserService createUserServiceRpc(Tracing tracing) {
        ConnectOptions connectOptions = new ConnectOptions();
        Filter tf = new TracingFilter(RpcTracing.create(tracing), Kind.CLIENT);

        ClientOptions option = new ClientOptions().setCodecId(ProtobufCodec.CODEC_ID).connect("localhost", 3301)
                .setConnectOptions(connectOptions).addFilter(tf);
        Client client = new Client(option);
        return client.importing(UserService.class);
    }

}
