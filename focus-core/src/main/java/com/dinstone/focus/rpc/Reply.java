/*
 * Copyright (C) 2019~2020 dinstone<dinstone@163.com>
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
package com.dinstone.focus.rpc;

import java.io.Serializable;

/**
 * call's result
 * 
 * @author guojinfei
 * 
 * @version 1.0.0.2014-6-23
 */
public class Reply implements Serializable {

    /**  */
    private static final long serialVersionUID = 1L;

    private int code;

    private String message;

    private Object data;

    private Attach attach = new Attach();

    public Reply() {
        super();
    }

    public Reply(int code, String message) {
        super();
        this.code = code;
        this.message = message;
    }

    public Reply(int code, Object data) {
        super();
        this.code = code;
        this.data = data;
    }

    public Reply(Object data) {
        super();
        this.data = data;
    }

    /**
     * the code to get
     * 
     * @return the code
     * 
     * @see Reply#code
     */
    public int getCode() {
        return code;
    }

    /**
     * the code to set
     * 
     * @param code
     * 
     * @see Reply#code
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * the message to get
     * 
     * @return the message
     * 
     * @see Reply#message
     */
    public String getMessage() {
        return message;
    }

    /**
     * the message to set
     * 
     * @param message
     * 
     * @see Reply#message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * the data to get
     * 
     * @return the data
     * 
     * @see Reply#data
     */
    public Object getData() {
        return data;
    }

    /**
     * the data to set
     * 
     * @param data
     * 
     * @see Reply#data
     */
    public void setData(Object data) {
        this.data = data;
    }

    /**
     * the attach to get
     * 
     * @return
     */
    public Attach attach() {
        return attach;
    }

    /**
     * the attach to set
     * 
     * @param other
     * 
     * @return
     */
    public Reply attach(Attach other) {
        if (other != null) {
            attach.putAll(other);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "{code=" + code + ", message=" + message + ", data=" + data + "}";
    }

}
