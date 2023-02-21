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
package com.dinstone.focus.transport.photon;

import com.dinstone.focus.transport.Connector;
import com.dinstone.focus.transport.ConnectorFactory;
import com.dinstone.focus.transport.ConnectOptions;

public class PhotonConnectorFactory implements ConnectorFactory {

    @Override
    public boolean appliable(ConnectOptions connectOptions) {
        return connectOptions instanceof PhotonConnectOptions
                || connectOptions == ConnectOptions.DEFAULT_CONNECT_OPTIONS;
    }

    @Override
    public Connector create(ConnectOptions connectOptions) {
        if (connectOptions == ConnectOptions.DEFAULT_CONNECT_OPTIONS) {
            connectOptions = new PhotonConnectOptions();
        }
        return new PhotonConnector((PhotonConnectOptions) connectOptions);
    }

}
