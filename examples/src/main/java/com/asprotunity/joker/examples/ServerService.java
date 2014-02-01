package com.asprotunity.joker.examples;

import com.asprotunity.joker.proxy.ServiceProxy;


public interface ServerService {

    String callServer(ServiceProxy<ClientService> caller, int arg);

    String[] emptyCall();

}
