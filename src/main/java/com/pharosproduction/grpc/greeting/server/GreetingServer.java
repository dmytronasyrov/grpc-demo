package com.pharosproduction.grpc.greeting.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GreetingServer {

  public static void main(String[] args) throws IOException, InterruptedException {
    System.out.println("gRPC server starting...");

    Server server = ServerBuilder.forPort(5000)
      .build();
    server.start();

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.out.println("Received Shutdown...");
      server.shutdown();
      System.out.println("Server Stopped");
    }));

    server.awaitTermination();
  }
}
