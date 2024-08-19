package edu.hit.fmpmm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoppeliaSimConfig {
    @Value("${coppelia.host}")
    private String host;

    @Value("${coppelia.port}")
    private int port;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
