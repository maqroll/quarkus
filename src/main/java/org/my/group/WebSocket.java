package org.my.group;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import org.eclipse.microprofile.reactive.messaging.Channel;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.ArrayList;
import java.util.List;

@ServerEndpoint("/wss/{id}")
@ApplicationScoped
public class WebSocket {

  // TODO: synchronization???
  private List<Session> sessions = new ArrayList<>();
  private Disposable subscription;

  @Inject
  @Channel("my-data-stream")
  Flowable<Double> prices;

  @OnOpen
  public void onOpen(Session session, @PathParam("id") String username) {
    sessions.add(session);
  }

  @OnClose
  public void onClose(Session session, @PathParam("id") String username) {
    sessions.remove(session);
  }

  @OnError
  public void onError(Session session, @PathParam("id") String username, Throwable throwable) {
    sessions.remove(session);
  }

  @OnMessage
  public void onMessage(String message, @PathParam("id") String username) {
  }

  @PostConstruct
  public void subscribe() {
    subscription = prices.subscribe((Double price) -> sessions.forEach(s -> {
      s.getAsyncRemote().sendObject(price, result -> {
        if (result.getException() != null) {
          System.out.println("Unable to send message: " + result.getException());
        }
      });
    }));
  }

  @PreDestroy
  public void cleanup() throws Exception {
    subscription.dispose();
  }

  private void broadcast(String message) {
    sessions.stream().forEach(s -> {
      s.getAsyncRemote().sendObject(message, result -> {
        if (result.getException() != null) {
          System.out.println("Unable to send message: " + result.getException());
        }
      });
    });
  }
}
