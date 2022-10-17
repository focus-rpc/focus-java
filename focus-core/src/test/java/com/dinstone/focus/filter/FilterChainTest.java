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

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CompletableFuture;

import org.junit.Test;

import com.dinstone.focus.config.ServiceConfig;
import com.dinstone.focus.invoke.InvokeHandler;
import com.dinstone.focus.protocol.Call;
import com.dinstone.focus.protocol.Reply;

public class FilterChainTest {

    @Test
    public void test() throws Exception {
        InvokeHandler lih = new InvokeHandler() {

            @Override
            public CompletableFuture<Reply> invoke(Call call) throws Exception {
                System.out.println("last InvokeHandler");
                return null;
            }
        };
        Filter first = new Filter() {

            @Override
            public CompletableFuture<Reply> invoke(FilterContext next, Call call) throws Exception {
                System.out.println("first filter before");
                CompletableFuture<Reply> r = next.invoke(call);
                System.out.println("first filter after");
                return r;
            }

            @Override
            public boolean matches(ServiceConfig serviceConfig) {
                // TODO Auto-generated method stub
                return false;
            }
        };
        Filter second = new Filter() {

            @Override
            public CompletableFuture<Reply> invoke(FilterContext next, Call call) throws Exception {
                System.out.println("second filter before");
                CompletableFuture<Reply> r = next.invoke(call);
                System.out.println("second filter after");
                return r;
            }

            @Override
            public boolean matches(ServiceConfig serviceConfig) {
                // TODO Auto-generated method stub
                return false;
            }
        };
        FilterChainHandler chain = new FilterChainHandler(new ServiceConfig(), lih).addFilter(first, second);

        CompletableFuture<Reply> reply = chain.invoke(null);
        assertEquals(null, reply);
    }

}
