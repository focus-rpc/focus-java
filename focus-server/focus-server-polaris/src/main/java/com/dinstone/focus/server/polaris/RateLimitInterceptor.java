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
package com.dinstone.focus.server.polaris;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import com.dinstone.focus.exception.ErrorCode;
import com.dinstone.focus.exception.InvokeException;
import com.dinstone.focus.invoke.Handler;
import com.dinstone.focus.invoke.Interceptor;
import com.dinstone.focus.protocol.Call;
import com.dinstone.focus.protocol.Reply;
import com.tencent.polaris.api.config.ConfigProvider;
import com.tencent.polaris.factory.ConfigAPIFactory;
import com.tencent.polaris.factory.config.ConfigurationImpl;
import com.tencent.polaris.factory.config.global.GlobalConfigImpl;
import com.tencent.polaris.factory.config.global.ServerConnectorConfigImpl;
import com.tencent.polaris.plugins.stat.prometheus.handler.PrometheusHandlerConfig;
import com.tencent.polaris.ratelimit.api.core.LimitAPI;
import com.tencent.polaris.ratelimit.api.rpc.QuotaRequest;
import com.tencent.polaris.ratelimit.api.rpc.QuotaResponse;
import com.tencent.polaris.ratelimit.api.rpc.QuotaResultCode;
import com.tencent.polaris.ratelimit.factory.LimitAPIFactory;

public class RateLimitInterceptor implements Interceptor {

    private static final String DEFAULT_NAMESPACE = "default";

    private LimitAPI limitAPI;

    public RateLimitInterceptor(String... addresses) {
        ConfigurationImpl configuration = (ConfigurationImpl) ConfigAPIFactory
                .defaultConfig(ConfigProvider.DEFAULT_CONFIG);

        final GlobalConfigImpl globalConfig = configuration.getGlobal();

        globalConfig.getStatReporter().setEnable(true);

        PrometheusHandlerConfig prometheusHandlerConfig = globalConfig.getStatReporter().getPluginConfig("prometheus",
                PrometheusHandlerConfig.class);
        prometheusHandlerConfig.setType("push");
        prometheusHandlerConfig.setAddress("119.91.66.223:9091");
        prometheusHandlerConfig.setPushInterval(10 * 1000L);
        globalConfig.getStatReporter().setPluginConfig("prometheus", prometheusHandlerConfig);

        ServerConnectorConfigImpl serverConnector = globalConfig.getServerConnector();
        serverConnector.setAddresses(Arrays.asList(addresses));

        limitAPI = LimitAPIFactory.createLimitAPIByConfig(configuration);
    }

    @Override
    public CompletableFuture<Reply> intercept(Call call, Handler handler) throws Exception {
        QuotaRequest quotaRequest = new QuotaRequest();
        // 设置需要进行限流的服务信息：设置命名空间信息
        quotaRequest.setNamespace(DEFAULT_NAMESPACE);
        // 设置需要进行限流的服务信息：设置服务名称信息
        quotaRequest.setService(call.getProvider());
        // 设置本次被调用的方法信息
        quotaRequest.setMethod(call.getMethod());
        // 设置本次的请求标签
        // quotaRequest.setArguments();
        // 设置需要申请的请求配额数量
        quotaRequest.setCount(1);

        QuotaResponse response = limitAPI.getQuota(quotaRequest);

        if (response.getCode() == QuotaResultCode.QuotaResultOk) {
            return handler.handle(call);
        } else {
            throw new InvokeException(ErrorCode.INVOKE_ERROR, "service is rate-limit");
        }
    }

}
