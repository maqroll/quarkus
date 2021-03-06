package org.my.group;

import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.Random;

@ApplicationScoped
public class PriceGenerator {
  private static final Logger LOG = Logger.getLogger(PriceGenerator.class);

  private Random random = new Random();

  // Kafka connector keeps getting messages even if it can't send them to Kafka
  // it seems to buffer them and send them to Kafka when connection is restablished (not in order).
  // If max-in-flight-requests=1 -> DROP messages if Kafka is not available.
  // If the available buffer is too small -> it doesn't report the error back (just write a message).
  // and the health-check keeps reporting ok.
  // This reactive-streams thing fills the same gap that channels in Go but in a totally different way.
  // Probability of generated-price dropping messages is not negligible: underneath ticks start on subscription
  // , not in request.
  // Error focus on consumer??
  @Outgoing("generated-price")
  public Multi<String> generate() {
    return Multi.createFrom().ticks().every(Duration.ofSeconds(5))
        .onSubscribe().invoke(sub -> LOG.debug("Received subscription: " + sub))
        .onRequest().invoke(req -> LOG.debug("Got a request: " + req))
        .onItem().invoke(i -> LOG.debug("Item: " + i))
        .onOverflow().invoke(t -> LOG.debug("Dropped " + t)).drop()
        .onFailure().invoke(t -> LOG.debug(t.getMessage())) // what failure??
        .map(tick -> tick.toString());
  }

}
