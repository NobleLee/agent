package com.alibaba.dubbo.performance.demo.agent.registry;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Endpoint {
    private final String host;
    private final List<Integer> ports;

    public AtomicInteger reqNum = new AtomicInteger(0);

    public Endpoint(String host, List<Integer> ports) {
        this.host = host;
        this.ports = ports;
    }

    public String getHost() {
        return host;
    }

    public List<Integer> getPort() {
        return ports;
    }

    public String toString() {
        return host + ":" + ports.toString();
    }

    public boolean equals(Object o) {
        if (!(o instanceof Endpoint)) {
            return false;
        }
        Endpoint other = (Endpoint) o;
        return other.host.equals(this.host);
    }

    public int hashCode() {
        return host.hashCode();
    }


}
