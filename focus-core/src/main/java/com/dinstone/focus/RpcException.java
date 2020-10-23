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
package com.dinstone.focus;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * RPC runtime exception.
 * 
 * @author guojf
 * 
 * @version 1.0.0.2013-10-28
 */
public class RpcException extends RuntimeException {

    /**  */
    private static final long serialVersionUID = 1L;

    private int code;

    private String stack;

    /**
     * @param message
     */
    public RpcException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * @param message
     * @param cause
     */
    public RpcException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * @param message
     * @param cause
     */
    public RpcException(int code, String message, String stack) {
        super(message);
        this.code = code;
        this.stack = stack;
    }

    /**
     * the code to get
     * 
     * @return the code
     * 
     * @see RpcException#code
     */
    public int getCode() {
        return code;
    }

    public String getStack() {
        return stack;
    }

    @Override
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    @Override
    public void printStackTrace(PrintStream s) {
        if (stack != null) {
            s.print(stack);
        } else {
            super.printStackTrace(s);
        }
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        if (stack != null) {
            s.print(stack);
        } else {
            super.printStackTrace(s);
        }
    }
}
