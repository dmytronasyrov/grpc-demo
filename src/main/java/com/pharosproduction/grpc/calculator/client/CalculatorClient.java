package com.pharosproduction.grpc.calculator.client;

import com.pharosproduction.grpc.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CalculatorClient {

  public static void main(String[] args) {
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 5000)
      .usePlaintext()
      .build();

//    CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);

//    SumRequest request = SumRequest.newBuilder()
//      .setFirstNumber(10)
//      .setSecondNumber(25)
//      .build();
//    SumResponse response = stub.sum(request);

//    long number = 5678902874823873498L;
//
//    PrimeNumberDecompositionRequest request = PrimeNumberDecompositionRequest.newBuilder()
//      .setNumber(number)
//      .build();
//    stub.primeNumberDecomposition(request)
//      .forEachRemaining(response -> {
//        System.out.println("PRIME IS: " + response.getPrimeFactor());
//      });

    CountDownLatch latch = new CountDownLatch(1);

    CalculatorServiceGrpc.CalculatorServiceStub client = CalculatorServiceGrpc.newStub(channel);
//    StreamObserver<AverageRequest> requestObserver = client.computeAverage(new StreamObserver<AverageResponse>() {
//      @Override
//      public void onNext(AverageResponse value) {
//        System.out.println("Received a response from the server");
//        System.out.println(value.getAverage());
//      }
//
//      @Override
//      public void onError(Throwable t) {
//
//      }
//
//      @Override
//      public void onCompleted() {
//        System.out.println("Server has completed sending us data");
//        latch.countDown();
//      }
//    });
//
//    for (int i = 0; i < 10000; i++) {
//      requestObserver.onNext(AverageRequest.newBuilder().setNumber(i).build());
//    }

    StreamObserver<FindMaxRequest> requestObserver = client.findMax(new StreamObserver<FindMaxResponse>() {
      @Override
      public void onNext(FindMaxResponse value) {
        System.out.println("Got new max from server: " + value.getMaximum());
      }

      @Override
      public void onError(Throwable t) {
        latch.countDown();
      }

      @Override
      public void onCompleted() {
        latch.countDown();
        System.out.println("Server is done sending messages");
      }
    });

    Arrays.asList(3, 5, 7, 9, 8, 30, 12).forEach(number -> {
      System.out.println("Sending number: " + number);

      requestObserver.onNext(FindMaxRequest.newBuilder()
        .setNumber(number)
        .build());

      try {
        latch.await(1000, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        e.printStackTrace();
      } finally {
        channel.shutdown();
      }
    });

    requestObserver.onCompleted();

    try {
      latch.await(3000, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      channel.shutdown();
    }
  }
}
