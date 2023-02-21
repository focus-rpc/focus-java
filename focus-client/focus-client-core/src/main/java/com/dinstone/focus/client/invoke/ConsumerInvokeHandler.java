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
package com.dinstone.focus.client.invoke;

import java.util.concurrent.CompletableFuture;

import com.dinstone.focus.config.ServiceConfig;
import com.dinstone.focus.invoke.FilterChainHandler;
import com.dinstone.focus.invoke.InvokeContext;
import com.dinstone.focus.invoke.InvokeHandler;
import com.dinstone.focus.protocol.Attach;
import com.dinstone.focus.protocol.Call;
import com.dinstone.focus.protocol.Reply;

/**
 * client-side service invoker.
 *
 * @author dinstone
 *
 * @version 1.0.0
 */
public class ConsumerInvokeHandler extends FilterChainHandler {

    public ConsumerInvokeHandler(ServiceConfig serviceConfig, InvokeHandler invokeHandler) {
        super(serviceConfig, invokeHandler);
    }

    public CompletableFuture<Reply> invoke(Call call) throws Exception {
        InvokeContext.pushContext();
        InvokeContext.getContext();
        try {
            call.attach().put(Attach.CONSUMER_KEY, serviceConfig.getEndpoint());

            return filterChain.invoke(call);
        } finally {
            InvokeContext.removeContext();
            InvokeContext.popContext();
        }
    }

}
