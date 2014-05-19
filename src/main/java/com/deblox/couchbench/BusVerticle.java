package com.deblox.couchbench;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;
import org.vertx.java.core.eventbus.EventBus;

public class BusVerticle extends Verticle {
    EventBus eventBus;

    public void start() {
        eventBus = vertx.eventBus();

        eventBus.registerHandler("bus-verticle", new Handler<Message<JsonObject>>() {
            public void handle(Message<JsonObject> message) {
                JsonObject reply = new JsonObject();
                message.reply(reply);
            }
        });
    }
}
