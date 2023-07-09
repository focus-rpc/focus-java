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
package com.dinstone.focus.client.starter;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import com.dinstone.focus.annotation.ServiceReference;
import com.dinstone.focus.client.FocusClient;
import com.dinstone.focus.client.ImportOptions;

public class FocusReferenceProcessor implements BeanPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithFields(bean.getClass(), field -> {
            ServiceReference autowired = AnnotationUtils.getAnnotation(field, ServiceReference.class);
            if (autowired != null) {
                FocusClient client = applicationContext.getBean(FocusClient.class);

                ReflectionUtils.makeAccessible(field);
                ReflectionUtils.setField(field, bean,
                        client.importing(field.getType(),
                                new ImportOptions(autowired.application(), autowired.service())
                                        .setTimeoutMillis(autowired.timeoutMillis())));
            }
        });
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}