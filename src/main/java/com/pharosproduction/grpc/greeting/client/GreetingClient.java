package com.pharosproduction.grpc.greeting.client;

import com.pharosproduction.grpc.greeting.*;

import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {

  public static void main(String[] args) throws SSLException {
    System.out.println("gRPC client starting...");

//    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 5000)
//      .usePlaintext()
//      .build();

    ManagedChannel channel = NettyChannelBuilder.forAddress("localhost", 5000)
      .sslContext(GrpcSslContexts.forClient().trustManager(new File("ssl/ca.crt")).build())
      .build();

//    unaryCall(channel);
//    streamingServer(channel);
//    streamingClient(channel);
//    streamingBiDir(channel);
    doUnaryWithDeadline(channel);
  }

  private static void unaryCall(ManagedChannel channel) {
    GreetingServiceGrpc.GreetingServiceBlockingStub client = GreetingServiceGrpc.newBlockingStub(channel);

    Greeting greeting = Greeting.newBuilder()
      .setFirstName("John")
      .setLastName("Doe")
      .build();

    GreetRequest request = GreetRequest.newBuilder()
      .setGreeting(greeting)
      .build();
    GreetResponse response = client.greet(request);
    System.out.println("RESPONSE IS: " + response.getResult());

    System.out.println("Shutting down channel");
    channel.shutdown();
  }

  private static void streamingServer(ManagedChannel channel) {
    GreetingServiceGrpc.GreetingServiceBlockingStub client = GreetingServiceGrpc.newBlockingStub(channel);

    Greeting greeting = Greeting.newBuilder()
      .setFirstName("John")
      .setLastName("Doe")
      .build();

    GreetRequest request = GreetRequest.newBuilder()
      .setGreeting(greeting)
      .build();
    client.greetManyTimes(request)
      .forEachRemaining(response -> {
        System.out.println(response.getResult());
      });

    System.out.println("Shutting down channel");
    channel.shutdown();
  }

  private static void streamingClient(ManagedChannel channel) {
    CountDownLatch latch = new CountDownLatch(1);

    GreetingServiceGrpc.GreetingServiceStub asyncClient = GreetingServiceGrpc.newStub(channel);
    StreamObserver<GreetRequest> requestObserver = asyncClient.longGreet(new StreamObserver<GreetResponse>() {
      @Override
      public void onNext(GreetResponse value) {
        System.out.println("Received a response from the server");
        System.out.println(value.getResult());
      }

      @Override
      public void onError(Throwable t) {

      }

      @Override
      public void onCompleted() {
        System.out.println("Server has completed sending us something");

        latch.countDown();
      }
    });

    System.out.println("Sending msg 1");

    Greeting greeting1 = Greeting.newBuilder()
      .setFirstName("John")
      .build();
    GreetRequest request1 = GreetRequest.newBuilder()
      .setGreeting(greeting1)
      .build();
    requestObserver.onNext(request1);

    System.out.println("Sending msg 2");

    Greeting greeting2 = Greeting.newBuilder()
      .setFirstName("Doe")
      .build();
    GreetRequest request2 = GreetRequest.newBuilder()
      .setGreeting(greeting2)
      .build();
    requestObserver.onNext(request2);

    System.out.println("Sending msg 3");

    Greeting greeting3 = Greeting.newBuilder()
      .setFirstName("Petja")
      .build();
    GreetRequest request3 = GreetRequest.newBuilder()
      .setGreeting(greeting3)
      .build();
    requestObserver.onNext(request3);

    requestObserver.onCompleted();

    try {
      latch.await(3000L, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      channel.shutdown();
    }
  }

  private static void streamingBiDir(ManagedChannel channel) {
    CountDownLatch latch = new CountDownLatch(1);

    GreetingServiceGrpc.GreetingServiceStub client = GreetingServiceGrpc.newStub(channel);
    StreamObserver<GreetRequest> observer = client.greetEveryone(new StreamObserver<GreetResponse>() {
      @Override
      public void onNext(GreetResponse value) {
        System.out.println("Response from server: " + value.getResult());
      }

      @Override
      public void onError(Throwable t) {
        latch.countDown();
      }

      @Override
      public void onCompleted() {
        System.out.println("Server is done sending data");
        latch.countDown();
      }
    });

    Arrays.asList("John", "Doe", "Vasja", "Petja")
      .forEach(name -> {
        System.out.println("Sending " + name);
        Greeting greeting = Greeting.newBuilder().setFirstName(name).build();
        GreetRequest request = GreetRequest.newBuilder().setGreeting(greeting).build();
        observer.onNext(request);
      });

    observer.onCompleted();

    try {
      latch.await(3000L, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      channel.shutdown();
    }
  }

  private static void doUnaryWithDeadline(ManagedChannel channel) {
    GreetingServiceGrpc.GreetingServiceBlockingStub stub = GreetingServiceGrpc.newBlockingStub(channel);

    try {
      System.out.println("Sending a request with a deadline 5000 ms");

      GreetDeadlineResponse response = stub.withDeadline(Deadline.after(5000, TimeUnit.MILLISECONDS))
        .greetDeadline(
          GreedDeadlineRequest.newBuilder()
            .setGreeting(
              Greeting.newBuilder().setFirstName("John").build()
            )
            .build()
        );

      System.out.println("Response 1: " + response.getResult());
    } catch (StatusRuntimeException e) {
      if (e.getStatus() == Status.DEADLINE_EXCEEDED) {
        System.out.println("Deadline has been exceeded");
      } else {
        e.printStackTrace();
      }
    }

    try {
      System.out.println("Sending a request with a deadline 100 ms");

      GreetDeadlineResponse response = stub.withDeadline(Deadline.after(100, TimeUnit.MILLISECONDS))
        .greetDeadline(
          GreedDeadlineRequest.newBuilder()
            .setGreeting(
              Greeting.newBuilder().setFirstName("John").build()
            )
            .build()
        );

      System.out.println("Response 2: " + response.getResult());
    } catch (StatusRuntimeException e) {
      if (e.getStatus() == Status.DEADLINE_EXCEEDED) {
        System.out.println("Deadline has been exceeded");
      } else {
        e.printStackTrace();
      }
    }
  }
}
