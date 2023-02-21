/*
 * Copyright (C) 2019~2023 dinstone<dinstone@163.com>
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

import com.dinstone.focus.example.ArithService;
import com.dinstone.focus.example.ArithServiceImpl;
import com.dinstone.focus.example.AuthenService;
import com.dinstone.focus.example.DemoService;
import com.dinstone.focus.example.DemoServiceImpl;
import com.dinstone.focus.example.OrderService;
import com.dinstone.focus.example.OrderServiceImpl;
import com.dinstone.focus.example.UserService;
import com.dinstone.focus.example.UserServiceServerImpl;
import com.dinstone.focus.filter.Filter;
import com.dinstone.focus.filter.Filter.Kind;
import com.dinstone.focus.serialze.json.JacksonSerializer;
import com.dinstone.focus.serialze.protobuf.ProtobufSerializer;
import com.dinstone.focus.serialze.protostuff.ProtostuffSerializer;
import com.dinstone.focus.telemetry.TelemetryFilter;
import com.dinstone.loghub.Logger;
import com.dinstone.loghub.LoggerFactory;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;

public class FocusServerTest {

    private static final Logger LOG = LoggerFactory.getLogger(FocusServerTest.class);

    public static void main(String[] args) {

        String serviceName = "focus.example.server";
        OpenTelemetry openTelemetry = getTelemetry(serviceName);
        Filter tf = new TelemetryFilter(openTelemetry, Kind.SERVER);

        ServerOptions serverOptions = new ServerOptions().listen("localhost", 3333).setEndpoint(serviceName)
                .addFilter(tf);
        // serverOptions.setSerializerType(ProtostuffSerializer.SERIALIZER_TYPE);

        FocusServer server = new FocusServer(serverOptions);

        server.exporting(UserService.class, new UserServiceServerImpl());
        server.exporting(DemoService.class, new DemoServiceImpl());

        // stuff
        server.exporting(OrderService.class, new OrderServiceImpl(null, null),
                new ExportOptions(OrderService.class.getName())
                        .setSerializerType(ProtostuffSerializer.SERIALIZER_TYPE));
        // json
        server.exporting(OrderService.class, new OrderServiceImpl(null, null),
                new ExportOptions("OrderService").setSerializerType(JacksonSerializer.SERIALIZER_TYPE));

        // export alias service
        server.exporting(AuthenService.class, new AuthenService(), "AuthenService", null);
        server.exporting(ArithService.class, new ArithServiceImpl(),
                new ExportOptions("ArithService").setSerializerType(ProtobufSerializer.SERIALIZER_TYPE));

        // server.start();
        LOG.info("server start");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.destroy();
        LOG.info("server stop");
    }

    private static OpenTelemetry getTelemetry(String serviceName) {
        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.of(AttributeKey.stringKey("service.name"), serviceName)));

        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                // .addSpanProcessor(BatchSpanProcessor.builder(ZipkinSpanExporter.builder().build()).build())
                .setResource(resource).build();

        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder().setTracerProvider(sdkTracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .buildAndRegisterGlobal();
        return openTelemetry;
    }

}
