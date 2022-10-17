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
package com.dinstone.focus.filter;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.dinstone.focus.config.ServiceConfig;
import com.dinstone.focus.invoke.InvokeHandler;
import com.dinstone.focus.protocol.Call;
import com.dinstone.focus.protocol.Reply;

public class FilterChainHandler implements FilterChain, InvokeHandler {

    private ServiceConfig serviceConfig;

    private FilterContext head;

    private FilterContext tail;

    public FilterChainHandler(ServiceConfig serviceConfig, InvokeHandler invokeHandler) {
        this.serviceConfig = serviceConfig;

        if (invokeHandler == null) {
            throw new IllegalArgumentException("invokeHandler is null");
        }
        this.tail = new FilterContext(this, new Filter() {

            @Override
            public CompletableFuture<Reply> invoke(FilterContext next, Call call) throws Exception {
                return invokeHandler.invoke(call);
            }

            @Override
            public boolean matches(ServiceConfig serviceConfig) {
                return true;
            }
        });
        this.head = new FilterContext(this, null);

        this.head.next = tail;
        this.tail.prev = head;
    }

    @Override
    public FilterChainHandler addFilter(Filter... filters) {
        addFilter(Arrays.asList(filters));
        return this;
    }

    @Override
    public FilterChainHandler addFilter(List<Filter> filters) {
        if (filters != null && !filters.isEmpty()) {
            FilterContext now = null;
            FilterContext last = tail.prev;
            for (Filter filter : filters) {
                if (!filter.matches(serviceConfig)) {
                    continue;
                }

                now = new FilterContext(this, filter);

                last.next = now;

                now.prev = last;
                now.next = tail;

                tail.prev = now;

                last = now;
            }
        }
        return this;
    }

    @Override
    public ServiceConfig getServiceConfig() {
        return serviceConfig;
    }

    public FilterContext getHead() {
        return head;
    }

    public FilterContext getTail() {
        return tail;
    }

    @Override
    public CompletableFuture<Reply> invoke(Call call) throws Exception {
        return head.invoke(call);
    }

}
