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
package com.dinstone.focus.server.consul;

import com.dinstone.focus.server.ResolverFactory;
import com.dinstone.focus.server.ResolverOptions;
import com.dinstone.focus.server.ServiceResolver;

public class ConsulResolverFactory implements ResolverFactory {

    @Override
    public boolean applicable(ResolverOptions options) {
        return options instanceof ConsulResolverOptions;
    }

    @Override
    public ServiceResolver create(ResolverOptions options) {
        return new ConsulServiceResolver((ConsulResolverOptions) options);
    }

}
