package com.pharosproduction.grpc.greeting.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.File;
import java.io.IOException;

public class GreetingServer {

  public static void main(String[] args) throws IOException, InterruptedException {
    System.out.println("gRPC server starting...");

    Server server = ServerBuilder.forPort(5000)
      .useTransportSecurity(
        new File("ssl/server.crt"),
        new File("ssl/server.pem")
      )
      .addService(new GreetServiceImpl())
      .addService(ProtoReflectionService.newInstance())
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
