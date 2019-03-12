package com.pharosproduction.grpc.calculator.server;

import com.pharosproduction.grpc.calculator.*;
import io.grpc.stub.StreamObserver;

public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {

  @Override
  public void sum(SumRequest request, StreamObserver<SumResponse> responseObserver) {
    SumResponse response = SumResponse.newBuilder()
      .setSumResult(request.getFirstNumber() + request.getSecondNumber())
      .build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void primeNumberDecomposition(PrimeNumberDecompositionRequest request, StreamObserver<PrimeNumberDecompositionResponse> responseObserver) {
    long number = request.getNumber();
    long divisor = 2L;

    while (number > 1) {
      if (number % divisor == 0) {
        number /= divisor;

        PrimeNumberDecompositionResponse response = PrimeNumberDecompositionResponse.newBuilder()
          .setPrimeFactor(divisor)
          .build();
        responseObserver.onNext(response);
      } else {
        divisor += 1L;
      }
    }

    responseObserver.onCompleted();
  }

  @Override
  public StreamObserver<AverageRequest> computeAverage(StreamObserver<AverageResponse> responseObserver) {
    StreamObserver<AverageRequest> requestObserver = new StreamObserver<AverageRequest>() {

      int sum = 0;
      int count = 0;

      @Override
      public void onNext(AverageRequest value) {
        sum += value.getNumber();
        count += 1;
      }

      @Override
      public void onError(Throwable t) {

      }

      @Override
      public void onCompleted() {
        double average = (double) sum / count;

        AverageResponse response = AverageResponse.newBuilder()
          .setAverage(average)
          .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
      }
    };

    return requestObserver;
  }

  @Override
  public StreamObserver<FindMaxRequest> findMax(StreamObserver<FindMaxResponse> responseObserver) {
    return new StreamObserver<FindMaxRequest>() {

      int currentMax = 0;

      @Override
      public void onNext(FindMaxRequest value) {
        int currNumber = value.getNumber();

        if (currNumber > currentMax) {
          currentMax = currNumber;

          responseObserver.onNext(FindMaxResponse.newBuilder()
            .setMaximum(currentMax)
            .build());
        }
      }

      @Override
      public void onError(Throwable t) {
        responseObserver.onCompleted();
      }

      @Override
      public void onCompleted() {
        responseObserver.onNext(FindMaxResponse.newBuilder()
          .setMaximum(currentMax)
          .build());

        responseObserver.onCompleted();
      }
    };
  }
}
