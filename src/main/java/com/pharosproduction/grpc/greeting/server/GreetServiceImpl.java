package com.pharosproduction.grpc.greeting.server;

import com.pharosproduction.grpc.greeting.GreetRequest;
import com.pharosproduction.grpc.greeting.GreetResponse;
import com.pharosproduction.grpc.greeting.Greeting;
import com.pharosproduction.grpc.greeting.GreetingServiceGrpc;
import io.grpc.stub.StreamObserver;

public class GreetServiceImpl extends GreetingServiceGrpc.GreetingServiceImplBase {

  @Override
  public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
    Greeting greeting = request.getGreeting();
    String firstName = greeting.getFirstName();
    String result = "Hello " + firstName;
    GreetResponse response = GreetResponse.newBuilder()
      .setResult(result)
      .build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void greetManyTimes(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
    String firstName = request.getGreeting()
      .getFirstName();

    try {
      for (int i = 0; i < 10; i++) {
        String result = "Hello " + firstName + ", response number: " + i;
        GreetResponse response = GreetResponse.newBuilder()
          .setResult(result)
          .build();
        responseObserver.onNext(response);

        Thread.sleep(1000L);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      responseObserver.onCompleted();
    }
  }

  @Override
  public StreamObserver<GreetRequest> longGreet(StreamObserver<GreetResponse> responseObserver) {
    return new StreamObserver<GreetRequest>() {

      String result = "";

      @Override
      public void onNext(GreetRequest value) {
        result += "Hello " + value.getGreeting().getFirstName() + "! ";
      }

      @Override
      public void onError(Throwable t) {

      }

      @Override
      public void onCompleted() {
        GreetResponse response = GreetResponse.newBuilder()
          .setResult(result)
          .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
      }
    };
  }

  @Override
  public StreamObserver<GreetRequest> greetEveryone(StreamObserver<GreetResponse> responseObserver) {
    return new StreamObserver<GreetRequest>() {
      @Override
      public void onNext(GreetRequest value) {
        String result = "Hello " + value.getGreeting().getFirstName();
        GreetResponse response = GreetResponse.newBuilder()
          .setResult(result)
          .build();

        responseObserver.onNext(response);
      }

      @Override
      public void onError(Throwable t) {

      }

      @Override
      public void onCompleted() {
        responseObserver.onCompleted();
      }
    };
  }
}
