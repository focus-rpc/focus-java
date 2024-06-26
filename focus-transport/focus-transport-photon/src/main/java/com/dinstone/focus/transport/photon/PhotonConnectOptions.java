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
package com.dinstone.focus.transport.photon;

import com.dinstone.focus.transport.ConnectOptions;

public class PhotonConnectOptions extends com.dinstone.photon.ConnectOptions implements ConnectOptions {

    private static final int DEFAULT_POOL_SIZE = 1;

    private int connectPoolSize = DEFAULT_POOL_SIZE;

    public PhotonConnectOptions() {
    }

    public PhotonConnectOptions(PhotonConnectOptions other) {
        super(other);
        connectPoolSize = other.connectPoolSize;
    }

    public int getConnectPoolSize() {
        return connectPoolSize;
    }

    public PhotonConnectOptions setConnectPoolSize(int connectPoolSize) {
        this.connectPoolSize = connectPoolSize;
        return this;
    }

}
