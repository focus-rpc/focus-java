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
package com.dinstone.focus.clutch.consul;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.dinstone.focus.clutch.ServiceDiscovery;
import com.dinstone.focus.clutch.ServiceInstance;
import com.dinstone.focus.clutch.ServiceInstanceSerializer;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.health.HealthServicesRequest;
import com.ecwid.consul.v1.health.model.HealthService;

public class ConsulServiceDiscovery implements ServiceDiscovery {

    private ConsulClient client;

    private ConsulClutchOptions config;

    private Map<String, ServiceCache> serviceCacheMap = new ConcurrentHashMap<>();

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {

        @Override
        public Thread newThread(Runnable taskt) {
            Thread t = new Thread(taskt, "consul-service-discovery");
            t.setDaemon(true);
            return t;
        }
    });

    public ConsulServiceDiscovery(ConsulClutchOptions clutchOptions) {
        this.config = clutchOptions;
        this.client = new ConsulClient(clutchOptions.getAgentHost(), clutchOptions.getAgentPort());
    }

    @Override
    public void destroy() {
        synchronized (serviceCacheMap) {
            for (ServiceCache serviceCache : serviceCacheMap.values()) {
                serviceCache.destroy();
            }
            serviceCacheMap.clear();
        }

        executorService.shutdownNow();
    }

    @Override
    public void cancel(ServiceInstance description) {
        synchronized (serviceCacheMap) {
            ServiceCache serviceCache = serviceCacheMap.get(description.getServiceName());
            if (serviceCache != null && serviceCache.decrement() <= 0) {
                serviceCache.destroy();
                serviceCacheMap.remove(description.getServiceName());
            }
        }
    }

    @Override
    public void listen(ServiceInstance description) throws Exception {
        synchronized (serviceCacheMap) {
            ServiceCache serviceCache = serviceCacheMap.get(description.getServiceName());
            if (serviceCache == null) {
                serviceCache = new ServiceCache(description, config).build();
                serviceCacheMap.put(description.getServiceName(), serviceCache);
            }
            serviceCache.increment();
        }
    }

    @Override
    public Collection<ServiceInstance> discovery(String name) throws Exception {
        ServiceCache serviceCache = serviceCacheMap.get(name);
        if (serviceCache != null) {
            return serviceCache.getProviders();
        }
        return null;
    }

    public class ServiceCache {

        private ServiceInstance serviceInstance;

        private ConsulClutchOptions clutchOptions;

        private ScheduledFuture<?> scheduledFuture;

        private AtomicInteger reference = new AtomicInteger();

        private ServiceInstanceSerializer serializer = new ServiceInstanceSerializer();

        private ConcurrentHashMap<String, ServiceInstance> providers = new ConcurrentHashMap<>();

        public ServiceCache(ServiceInstance serviceInstance, ConsulClutchOptions clutchOptions) {
            this.clutchOptions = clutchOptions;
            this.serviceInstance = serviceInstance;
        }

        public ServiceCache build() {
            this.scheduledFuture = executorService.scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {
                    try {
                        freshProvidors();
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }, clutchOptions.getInterval(), clutchOptions.getInterval(), TimeUnit.SECONDS);

            try {
                freshProvidors();
            } catch (Exception e) {
                // ignore
            }

            return this;
        }

        protected void freshProvidors() throws Exception {
            HealthServicesRequest hr = HealthServicesRequest.newBuilder().setPassing(true).build();
            List<HealthService> healthServices = client.getHealthServices(serviceInstance.getServiceName(), hr)
                    .getValue();
            for (HealthService healthService : healthServices) {
                List<String> tags = healthService.getService().getTags();
                ServiceInstance description = null;
                if (tags != null && tags.size() > 0) {
                    description = serializer.deserialize(tags.get(0).getBytes("utf-8"));
                }

                if (description != null) {
                    providers.put(description.getInstanceCode(), description);
                }
            }
        }

        public Collection<ServiceInstance> getProviders() {
            return providers.values();
        }

        public int increment() {
            return reference.incrementAndGet();
        }

        public int decrement() {
            return reference.decrementAndGet();
        }

        public void destroy() {
            if (scheduledFuture != null) {
                scheduledFuture.cancel(false);
            }
        }

    }

}