package org.my.group;

import io.smallrye.reactive.messaging.annotations.Broadcast;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PriceConverter {

  private static final double CONVERSION_RATE = 0.88;

  @Incoming("prices")
  @Outgoing("my-data-stream")
  @Broadcast
  public String process(String priceInUsd) {
    return Double.valueOf(Integer.valueOf(priceInUsd) * CONVERSION_RATE).toString();
  }

}
