/*
 * Copyright (C) 2018~2020 dinstone<dinstone@163.com>
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
package com.dinstone.focus.server.invoke;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.dinstone.focus.RpcException;
import com.dinstone.focus.binding.ImplementBinding;
import com.dinstone.focus.exception.ExcptionHelper;
import com.dinstone.focus.invoke.InvokeHandler;
import com.dinstone.focus.proxy.ServiceProxy;
import com.dinstone.focus.rpc.Call;
import com.dinstone.focus.rpc.Reply;

public class LocalInvokeHandler implements InvokeHandler {

    private ImplementBinding implementBinding;

    public LocalInvokeHandler(ImplementBinding implementBinding) {
        this.implementBinding = implementBinding;
    }

    @Override
    public Reply invoke(Call call) throws Exception {
        ServiceProxy<?> wrapper = implementBinding.lookup(call.getService(), call.getGroup());
        if (wrapper == null) {
            throw new RpcException(404, "unkown service: " + call.getService() + "[" + call.getGroup() + "]");
        }

        try {
            Class<?>[] paramTypes = getParamTypes(call.getParams(), call.getParamTypes());
            Method method = wrapper.getService().getDeclaredMethod(call.getMethod(), paramTypes);
            Object resObj = method.invoke(wrapper.getTarget(), call.getParams());
            return new Reply(resObj);
        } catch (RpcException e) {
            throw e;
        } catch (NoSuchMethodException e) {
            String message = "unkown method: [" + call.getGroup() + "]" + call.getService() + "." + call.getMethod();
            throw new RpcException(405, message, e);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            String message = "illegal access: [" + call.getGroup() + "]" + call.getService() + "." + call.getMethod();
            throw new RpcException(502, message, e);
        } catch (InvocationTargetException e) {
            Throwable te = ExcptionHelper.getTargetException(e);
            String message = "service exception: " + call.getGroup() + "]" + call.getService() + "." + call.getMethod();
            throw new RpcException(500, message, te);
        } catch (Throwable e) {
            String message = "service exception: " + call.getGroup() + "]" + call.getService() + "." + call.getMethod();
            throw new RpcException(509, message, e);
        }
    }

    private Class<?>[] getParamTypes(Object[] params, Class<?>[] paramTypes) {
        if (paramTypes == null && params != null) {
            paramTypes = parseParamTypes(params);
        }
        return paramTypes;
    }

    protected Class<?>[] getParamTypes(Call call) {
        Class<?>[] paramTypes = call.getParamTypes();
        Object[] params = call.getParams();
        if (paramTypes == null && params != null) {
            paramTypes = parseParamTypes(params);
        }
        return paramTypes;
    }

    private Class<?>[] parseParamTypes(Object[] args) {
        Class<?>[] cs = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            cs[i] = arg.getClass();
        }

        return cs;
    }

}
