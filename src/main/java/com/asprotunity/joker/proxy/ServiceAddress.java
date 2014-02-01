package com.asprotunity.joker.proxy;

public class ServiceAddress {
    final public String hostName;
    final public int port;
    final public String serviceName;

    public ServiceAddress(String hostName, int port, String serviceName) {
        this.hostName = hostName;
        this.port = port;
        this.serviceName = serviceName;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        ServiceAddress otherAddress = (ServiceAddress) other;

        return port == otherAddress.port &&
                hostName.equals(otherAddress.hostName) &&
                serviceName.equals(otherAddress.serviceName);
    }

    @Override
    public int hashCode() {
        int result = hostName.hashCode();
        result = 31 * result + port;
        result = 31 * result + serviceName.hashCode();
        return result;
    }
}
