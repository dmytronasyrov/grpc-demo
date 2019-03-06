package com.pharosproduction.grpc.greeting.client;

import com.pharosproduction.grpc.greeting.DummyServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GreetingClient {

  public static void main(String[] args) {
    System.out.println("gRPC client starting...");

    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 5000)
      .usePlaintext()
      .build();

    DummyServiceGrpc.DummyServiceBlockingStub syncClient = DummyServiceGrpc.newBlockingStub(channel);

    System.out.println("Shutting down channel");
    channel.shutdown();
  }
}
