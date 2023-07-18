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
package com.dinstone.focus.exception;

public enum ErrorCode {

    UNKOWN_ERROR(599), INVOKE_ERROR(500), TIMEOUT_ERROR(506), //
    CODEC_ERROR(201), SERVICE_ERROR(202), METHOD_ERROR(203), //
    DECLARED_ERROR(301), UNDECLARED_ERROR(302), RUNTIME_ERROR(303), PARAM_ERROR(304), ACCESS_ERROR(305);

    private int code;

    private ErrorCode(int value) {
        this.code = value;
    }

    public int value() {
        return code;
    }

    public static ErrorCode valueOf(int code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.code == code) {
                return errorCode;
            }
        }
        return UNKOWN_ERROR;
    }
}
