package com.deblox.couchbench;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import com.deblox.couchbench.Util;
import com.deblox.couchbench.User;

/**
 * Created by marzubus on 18/05/14.
 */

public class QueryTests extends Verticle {

    JsonObject config;

    // timers
    long startTime;
    long endTime;
    long timeEnded;
    Integer count = 0;
    Integer count_max = 1;

    @Override
    public void start() {

        config = new JsonObject();
        config.putString("address", "vertx.couchbase.sync");
        config.putString("couchbase.nodelist", "localhost:8091");
        config.putString("couchbase.bucket", "ivault");
        config.putString("couchbase.bucket.password", "");
        config.putNumber("couchbase.num.clients", 1);


        System.out.println("\n\n\nDeploying Couchbase Module from m2\n\n");

        container.deployModule("com.scalabl3~vertxmods.couchbase~1.0.0-final", config, 1, new AsyncResultHandler<String>() {

            @Override
            public void handle(AsyncResult<String> asyncResult) {

                // Deployment is asynchronous and this this handler will be called when it's complete (or failed)
                if (asyncResult.failed()) {
                    container.logger().error(asyncResult.cause());
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // If deployed correctly then start the tests!

                System.out.println("\n\n\nDeployed CouchBench Verticle, begin the benchmark\n\n");

                keyBenchmark();


            }
        });




    }

    // used to count async results and finalize tests
    public void count() {
        count=count+1;
        if (count > count_max-1) {
            endTime = System.currentTimeMillis();
            timeEnded =  ((endTime-startTime) /1000);
            System.out.println("rate achieved: " + (count_max/timeEnded) + " msgs/ps");
            count_max=1;
            count=0;
            System.exit(0);
        }
    }

    public void query_key(Integer i) {
            JsonObject request = new JsonObject().putString("op", "QUERY")
                    .putString("design_doc", "users")
                    .putString("view_name", "users")
                    .putString("key", "user" + i)
                    .putBoolean("include_docs", true)
                    .putBoolean("ack", true);

            //System.out.println("sending message to address: " + config.getString("address"));

            vertx.eventBus().send(config.getString("address"), request, new Handler<Message<JsonObject>>() {

                @Override
                public void handle(final Message<JsonObject> reply) {
                    try {
                        //System.out.println("reply was: " + reply.toString());

                        User u = (User)Util.decode(reply.body()
                                .getObject("response")
                                .getObject("response")
                                .getArray("result").get(0)
                                .toString(), User.class );
                        //System.out.println("created user " + u.toString());
                        if ( u.getPassword().equals("somepassword")) {
                            count();
                        } else {
                            System.out.println("Error, password missmatch, check your data: " + u.getPassword() + " : " + u.toString());

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                    }
                }
            });
    }


    public void keyBenchmark() {
        startTime = System.currentTimeMillis();
        endTime = 0;

        count_max=10000;
        System.out.println("firing off queries");
        for(int i=0; i < count_max; i++) {
            query_key(i);
        }
        System.out.println("done, waiting for async");

    }

}
