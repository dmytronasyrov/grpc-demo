package com.pharosproduction.grpc.blog.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.IOException;

public class BlogServer {

  // mongod --dbpath data
  public static void main(String[] args) throws InterruptedException, IOException {
    Server server = ServerBuilder.forPort(5000)
      .addService(new BlogServiceImpl())
      .addService(ProtoReflectionService.newInstance())
      .build();
    server.start();

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.out.println("Received shutdown request");
      server.shutdown();
      System.out.println("Successfully stopped the server");
    }));

    server.awaitTermination();
  }
}
