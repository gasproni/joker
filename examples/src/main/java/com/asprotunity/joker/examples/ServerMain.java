package com.asprotunity.joker.examples;

import com.asprotunity.joker.server.HTTPBroker;

public class ServerMain {

    public static void main(String[] args) {

        HTTPBroker broker = new HTTPBroker(9999);
        broker.start();
        broker.registerService("server", new ServerServiceImpl(),
                ServerService.class);
        broker.join();
    }
}
