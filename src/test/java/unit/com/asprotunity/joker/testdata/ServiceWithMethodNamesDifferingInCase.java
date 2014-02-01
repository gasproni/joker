package com.asprotunity.joker.testdata;

public class ServiceWithMethodNamesDifferingInCase implements ServiceInterfaceWithMethodNamesDifferingInCase {
    @Override
    public void call(int i, long l) {

    }

    @Override
    public void CALL(int i, long l) {

    }
}
