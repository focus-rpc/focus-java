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
package com.dinstone.focus.server.photon;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.dinstone.focus.codec.CodecException;
import com.dinstone.focus.compress.Compressor;
import com.dinstone.focus.config.MethodConfig;
import com.dinstone.focus.config.ServiceConfig;
import com.dinstone.focus.exception.InvokeException;
import com.dinstone.focus.protocol.Call;
import com.dinstone.focus.protocol.Reply;
import com.dinstone.focus.serialize.Serializer;
import com.dinstone.focus.server.ExecutorSelector;
import com.dinstone.focus.server.binding.ImplementBinding;
import com.dinstone.photon.Connection;
import com.dinstone.photon.MessageProcessor;
import com.dinstone.photon.message.Headers;
import com.dinstone.photon.message.Request;
import com.dinstone.photon.message.Response;
import com.dinstone.photon.message.Response.Status;
import com.dinstone.photon.utils.ByteStreamUtil;

public final class FocusMessageProcessor extends MessageProcessor {
    private final ImplementBinding implementBinding;
    private final ExecutorSelector executorSelector;

    public FocusMessageProcessor(ImplementBinding implementBinding, ExecutorSelector executorSelector) {
        this.implementBinding = implementBinding;
        this.executorSelector = executorSelector;
    }

    @Override
    public void process(Connection connection, Request request) {
        Executor executor = null;
        if (executorSelector != null) {
            Headers headers = request.headers();
            String g = headers.get(Call.GROUP_KEY);
            String s = headers.get(Call.SERVICE_KEY);
            String m = headers.get(Call.METHOD_KEY);
            executor = executorSelector.select(g, s, m);
        }
        if (executor != null) {
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    invoke(connection, request);
                }
            });
        } else {
            invoke(connection, request);
        }
    }

    private void invoke(Connection connection, Request request) {
        InvokeException exception = null;
        try {
            // check request timeout
            if (request.isTimeout()) {
                throw new InvokeException(308, "request timeout");
            }

            Headers headers = request.headers();
            // check service
            String group = headers.get(Call.GROUP_KEY);
            String service = headers.get(Call.SERVICE_KEY);
            ServiceConfig serviceConfig = implementBinding.lookup(service, group);
            if (serviceConfig == null) {
                throw new NoSuchMethodException("unkown service: " + service + "[" + group + "]");
            }

            // check method
            String methodName = headers.get(Call.METHOD_KEY);
            MethodConfig methodConfig = serviceConfig.getMethodConfig(methodName);
            if (methodConfig == null) {
                throw new NoSuchMethodException("unkown method: " + service + "[" + group + "]." + methodName);
            }

            // decode call from request
            Call call = decode(request, serviceConfig, methodConfig);

            // invoke call
            CompletableFuture<Reply> replyFuture = serviceConfig.getHandler().invoke(call);
            replyFuture.whenComplete((reply, error) -> {
                if (error != null) {
                    errorHandle(connection, request, error);
                } else {
                    // encode reply to response
                    Response response = encode(reply, serviceConfig, methodConfig);
                    response.setMsgId(request.getMsgId());

                    // send response with reply
                    connection.sendMessage(response);
                }
            });

            return;
        } catch (InvokeException e) {
            exception = e;
        } catch (CodecException e) {
            exception = new InvokeException(201, e);
        } catch (IllegalArgumentException e) {
            exception = new InvokeException(202, e);
        } catch (IllegalAccessException e) {
            exception = new InvokeException(203, e);
        } catch (NoSuchMethodException e) {
            exception = new InvokeException(204, e);
        } catch (Throwable e) {
            exception = new InvokeException(309, e);
        }

        if (exception != null) {
            errorHandle(connection, request, exception);
        }
    }

    private Response encode(Reply reply, ServiceConfig serviceConfig, MethodConfig methodConfig) {
        Response response = new Response();
        if (reply.isError()) {
            response.setStatus(Status.FAILURE);
            try {
                InvokeException exception = (InvokeException) reply.getData();
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                ByteStreamUtil.writeInt(bao, exception.getCode());
                ByteStreamUtil.writeString(bao, exception.getMessage());
                response.setContent(bao.toByteArray());
            } catch (IOException e) {
                throw new CodecException("serialize encode error: " + methodConfig.getMethodName(), e);
            }
        } else {
            response.setStatus(Status.SUCCESS);
            byte[] content = null;
            if (reply.getData() != null) {
                try {
                    Serializer serializer = serviceConfig.getSerializer();
                    content = serializer.encode(reply.getData(), methodConfig.getReturnType());
                    reply.attach().put(Serializer.TYPE_KEY, serializer.serializerType());
                } catch (IOException e) {
                    throw new CodecException("serialize encode error: " + methodConfig.getMethodName(), e);
                }

                Compressor compressor = serviceConfig.getCompressor();
                if (compressor != null && content.length > serviceConfig.getCompressThreshold()) {
                    try {
                        content = compressor.encode(content);
                        reply.attach().put(Compressor.TYPE_KEY, compressor.compressorType());
                    } catch (IOException e) {
                        throw new CodecException("compress encode error: " + methodConfig.getMethodName(), e);
                    }
                }
            }
            response.setContent(content);
        }
        response.headers().setAll(reply.attach());
        return response;
    }

    private Call decode(Request request, ServiceConfig serviceConfig, MethodConfig methodConfig) {
        Object value;
        Headers headers = request.headers();
        byte[] content = request.getContent();
        if (content == null || content.length == 0) {
            value = null;
        } else {
            String compressorType = headers.get(Compressor.TYPE_KEY);
            Compressor compressor = serviceConfig.getCompressor();
            if (compressor != null && compressorType != null) {
                try {
                    content = compressor.decode(content);
                } catch (IOException e) {
                    throw new CodecException("compress decode error: " + methodConfig.getMethodName(), e);
                }
            }

            try {
                Serializer serializer = serviceConfig.getSerializer();
                Class<?> contentType = methodConfig.getParamType();
                value = serializer.decode(content, contentType);
            } catch (IOException e) {
                throw new CodecException("serialize decode error: " + methodConfig.getMethodName(), e);
            }
        }

        Call call = new Call();
        call.setGroup(headers.get(Call.GROUP_KEY));
        call.setService(headers.get(Call.SERVICE_KEY));
        call.setMethod(headers.get(Call.METHOD_KEY));
        call.setTimeout(request.getTimeout());
        call.attach().putAll(headers);
        call.setParameter(value);
        return call;
    }

    private void errorHandle(Connection connection, Request request, Throwable error) {
        InvokeException exception;
        if (error instanceof InvokeException) {
            exception = (InvokeException) error;
        } else {
            exception = new InvokeException(99, error);
        }
        // send response with exception
        Response response = new Response();
        response.setMsgId(request.getMsgId());
        response.setStatus(Status.FAILURE);
        try {
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ByteStreamUtil.writeInt(bao, exception.getCode());
            ByteStreamUtil.writeString(bao, exception.getMessage());
            response.setContent(bao.toByteArray());
        } catch (IOException e) {
            throw new CodecException("serialize encode error", e);
        }
        connection.sendMessage(response);
    }
}