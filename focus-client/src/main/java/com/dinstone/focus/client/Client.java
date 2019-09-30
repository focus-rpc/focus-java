/*
 * Copyright (C) 2013~2017 dinstone<dinstone@163.com>
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

package com.dinstone.focus.client;

import java.net.InetSocketAddress;
import java.util.List;

import com.dinstone.focus.binding.DefaultReferenceBinding;
import com.dinstone.focus.binding.ReferenceBinding;
import com.dinstone.focus.client.invoke.LocationInvokeHandler;
import com.dinstone.focus.client.invoke.RemoteInvokeHandler;
import com.dinstone.focus.client.transport.ConnectionManager;
import com.dinstone.focus.endpoint.ServiceImporter;
import com.dinstone.focus.filter.FilterChain;
import com.dinstone.focus.invoke.InvokeHandler;
import com.dinstone.focus.invoke.ServiceInvoker;
import com.dinstone.focus.proxy.ServiceProxy;
import com.dinstone.focus.proxy.ServiceProxyFactory;
import com.dinstone.focus.registry.LocalRegistryFactory;
import com.dinstone.focus.registry.RegistryFactory;
import com.dinstone.focus.registry.ServiceDiscovery;

public class Client implements ServiceImporter {

    private ClientOptions clientOptions;

    private ServiceDiscovery serviceDiscovery;

    private ReferenceBinding referenceBinding;

    private ConnectionManager connectionManager;

    private ServiceProxyFactory serviceProxyFactory;

    public Client(ClientOptions clientOption) {
        checkAndInit(clientOption);
    }

    private void checkAndInit(ClientOptions clientOptions) {
        if (clientOptions == null) {
            throw new IllegalArgumentException("clientOptions is null");
        }
        this.clientOptions = clientOptions;

        // check transport provider
        this.connectionManager = new ConnectionManager(clientOptions);

        RegistryFactory registryFactory = new LocalRegistryFactory();
        this.serviceDiscovery = registryFactory.createServiceDiscovery(clientOptions.getRegistryConfig());

        this.referenceBinding = new DefaultReferenceBinding(clientOptions, serviceDiscovery);

        this.serviceProxyFactory = new ServiceProxyFactory(new ServiceInvoker(createInvokeHandler()));
    }

    private InvokeHandler createInvokeHandler() {
        RemoteInvokeHandler rih = new RemoteInvokeHandler(connectionManager);
        List<InetSocketAddress> addresses = clientOptions.getServiceAddresses();
        LocationInvokeHandler lih = new LocationInvokeHandler(rih, referenceBinding, addresses);
        return new FilterChain(lih, clientOptions.getFilters());
    }

    @Override
    public void destroy() {
        connectionManager.destroy();
        referenceBinding.destroy();

        if (serviceDiscovery != null) {
            serviceDiscovery.destroy();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.jrpc.endpoint.ServiceImporter#importService(java.lang.Class)
     */
    @Override
    public <T> T importing(Class<T> sic) {
        return importing(sic, "");
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.jrpc.endpoint.ServiceImporter#importService(java.lang.Class,
     *      java.lang.String)
     */
    @Override
    public <T> T importing(Class<T> sic, String group) {
        return importing(sic, group, clientOptions.getDefaultTimeout());
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.dinstone.jrpc.endpoint.ServiceImporter#importService(java.lang.Class,
     *      java.lang.String, int)
     */
    @Override
    public <T> T importing(Class<T> sic, String group, int timeout) {
        if (group == null) {
            group = "";
        }
        if (timeout <= 0) {
            timeout = clientOptions.getDefaultTimeout();
        }

        try {
            ServiceProxy<T> wrapper = serviceProxyFactory.create(sic, group, timeout, null);
            referenceBinding.binding(wrapper);
            return wrapper.getProxy();
        } catch (Exception e) {
            throw new RuntimeException("can't import service", e);
        }
    }

}
