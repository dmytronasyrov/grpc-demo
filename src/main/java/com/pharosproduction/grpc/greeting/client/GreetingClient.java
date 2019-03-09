package com.pharosproduction.grpc.greeting.client;

import com.pharosproduction.grpc.greeting.*;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {

  public static void main(String[] args) {
    System.out.println("gRPC client starting...");

    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 5000)
      .usePlaintext()
      .build();

//    unaryCall(channel);
//    streamingServer(channel);
//    streamingClient(channel);
    streamingBiDir(channel);
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
}
