/*
 * Copyright (C) 2019~2024 dinstone<dinstone@163.com>
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
package com.dinstone.focus.client.proxy;

import java.lang.reflect.Proxy;

import com.dinstone.focus.config.ServiceConfig;

public class JdkProxyFactory implements ProxyFactory {

    @Override
    public <T> T create(Class<T> sic, ServiceConfig serviceConfig) {
        if (!sic.isInterface()) {
            throw new IllegalArgumentException(sic.getName() + " is not interface");
        }

        ProxyHandler handler = new ProxyHandler(serviceConfig);
        return sic.cast(Proxy.newProxyInstance(sic.getClassLoader(), new Class[] { sic }, handler));
    }

}
