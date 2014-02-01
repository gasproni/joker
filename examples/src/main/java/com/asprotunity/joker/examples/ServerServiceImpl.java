package com.asprotunity.joker.examples;

import com.asprotunity.joker.proxy.ServiceProxy;

public class ServerServiceImpl implements ServerService {
    @Override
    public String callServer(ServiceProxy<ClientService> caller, int arg) {
        return caller.service().call("This is the server calling: ") + arg;
    }

    @Override
    public String[] emptyCall() {
        return new String[]{"Hey!!", "I'm", "here!"};
    }
}
