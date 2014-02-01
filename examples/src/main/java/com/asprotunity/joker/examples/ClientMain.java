package com.asprotunity.joker.examples;

import com.asprotunity.joker.client.HTTPServiceProxyMaker;
import com.asprotunity.joker.proxy.ServiceAddress;
import com.asprotunity.joker.proxy.ServiceProxy;
import com.asprotunity.joker.server.HTTPBroker;

public class ClientMain {

    public static void main(String[] args) {

        HTTPBroker broker = new HTTPBroker(6666);
        broker.start();

        ServiceProxy<ClientService> localService = broker.registerService("client",
                new ClientServiceImpl(),
                ClientService.class);

        HTTPServiceProxyMaker proxyMaker = new HTTPServiceProxyMaker();

        ServiceProxy<ServerService> server = proxyMaker.make(new ServiceAddress("localhost", 9999, "server"),
                ServerService.class);

        for (int i = 0; i < 1000; ++i) {
            System.out.println(server.service().callServer(localService, i));
        }

        broker.stop();

    }
}
