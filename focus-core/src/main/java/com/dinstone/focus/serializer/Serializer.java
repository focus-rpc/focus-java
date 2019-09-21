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
package com.dinstone.focus.serializer;

/**
 * Serialize java object to byte array and Deserialize byte array to java
 * object.
 *
 * @author guojinfei
 * @version 1.0.0.2014-7-29
 */
public interface Serializer {

    /**
     * The serializer name. Each serializer must have a unique name. This is used to
     * identify a serializer when sending a message and for unregistering
     * serializers.
     *
     * @return the name
     */
    String name();

    public <T> byte[] serialize(T data) throws Exception;

    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws Exception;

    public <T> T deserialize(byte[] bytes, int offset, int length, Class<T> clazz) throws Exception;

}
