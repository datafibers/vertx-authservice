package com.deblox.couchbench;

import org.vertx.java.platform.Verticle;

public class Server extends Verticle {
    public void start() {
        container.deployVerticle("ProcessorVerticle.java", 1);
        container.deployVerticle("HttpVerticle.java", 16);
    }
}
