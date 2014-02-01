package com.asprotunity.joker.testdata;

public class ServiceWithOverloading implements ServiceInterfaceWithOverloading {
    @Override
    public void call(int i) {
    }

    @Override
    public void call(int i, long l) {
    }
}
