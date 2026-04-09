package org.evolve.auth.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.evolve.auth")
public class AuthGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthGatewayApplication.class, args);
    }
}
