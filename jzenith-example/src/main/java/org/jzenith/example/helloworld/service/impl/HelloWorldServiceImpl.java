package org.jzenith.example.helloworld.service.impl;

import org.jzenith.example.helloworld.service.HelloWorldService;

public class HelloWorldServiceImpl implements HelloWorldService {

    @Override
    public String getResponse() {
        return "Hello World";
    }
}
