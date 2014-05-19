package com.deblox.couchbench;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class BusClient extends Verticle  {
    private long start;
    private int count = 0;

    // This determines the degree of pipelining
    private static final int CREDITS_BATCH = 2000;

    // Number of connections to create
    private static final int MAX_CONNS = 10;

    private int requestCredits = CREDITS_BATCH;
    private EventBus eb;

    public void start() {
        eb = vertx.eventBus();
        makeRequest();
    }

    private void makeRequest() {
        if (start == 0) {
            start = System.currentTimeMillis();
        }
        while (requestCredits > 0) {
            JsonObject message = new JsonObject();

            eb.send("bus-verticle", message, new Handler<Message<JsonObject>>() {
                public void handle(Message<JsonObject> message) {
                    count++;
                    if (count % 2000 == 0) {
                        eb.send("rate-counter", count);
                        count = 0;
                    }
                    requestCredits++;
                    makeRequest();
                }
            });
            requestCredits--;
        }
    }
}
