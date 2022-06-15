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
package com.dinstone.focus.example;

public class DemoServiceImpl implements DemoService {

    @Override
    public String hello(String name) {
        if (name == null || name.isEmpty()) {
            throw new RuntimeException("name is empty");
        }
        return "hi " + name;
    }

    @Override
    public String hello(String name, int age) {
        if (name == null || name.isEmpty()) {
            throw new RuntimeException("name is empty");
        }
        return "Hello " + name + ",age is " + age;
    }

}
