package com.pharosproduction.grpc.blog.client;

import com.proto.blog.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class BlogClient {

  public static void main(String[] args) {
    BlogClient main = new BlogClient();
    main.run();
  }

  private void run() {
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 5000)
      .usePlaintext()
      .build();
    BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);

    Blog blog = Blog.newBuilder()
      .setAuthorId("John")
      .setTitle("Doe")
      .setContent("Hello world")
      .build();

    CreateBlogRequest createRequest = CreateBlogRequest.newBuilder()
      .setBlog(blog)
      .build();
    CreateBlogResponse createResponse = blogClient.createBlog(createRequest);
    System.out.println("Received create blog response: " + createResponse.toString());

    String blogId = createResponse.getBlog().getId();
    ReadBlogRequest readRequest = ReadBlogRequest.newBuilder()
      .setBlogId(blogId)
      .build();
    ReadBlogResponse readResponse = blogClient.readBlog(readRequest);
    System.out.println("Received read blog response: " + readResponse.toString());

    try {
      ReadBlogRequest readRequestWrong = ReadBlogRequest.newBuilder()
        .setBlogId("fakeId")
        .build();
      ReadBlogResponse readResponseWrong = blogClient.readBlog(readRequestWrong);
      System.out.println("Received read blog wrong response: " + readResponseWrong.toString());
    } catch (StatusRuntimeException e) {
      System.out.println("Wrong ID" + e.getMessage());
    }

    try {
      ReadBlogRequest readRequestFake = ReadBlogRequest.newBuilder()
        .setBlogId("5c8cd2ef1c9585215f9549ae")
        .build();
      ReadBlogResponse readResponseFake = blogClient.readBlog(readRequestFake);
      System.out.println("Received read blog fake response: " + readResponseFake.toString());
    } catch (StatusRuntimeException e) {
      System.out.println("Fake ID" + e.getMessage());
    }

    Blog newBlog = Blog.newBuilder()
      .setId(blogId)
      .setAuthorId("test")
      .setTitle("Doe")
      .setContent("Updated content")
      .build();
    UpdateBlogResponse response = blogClient.updateBlog(UpdateBlogRequest.newBuilder().setBlog(newBlog).build());
    System.out.println("Updated blog:" + response.getBlog().toString());

//    System.out.println("Deleting blog");
//    DeleteBlogResponse deleteBlogResponse = blogClient.deleteBlog(
//      DeleteBlogRequest.newBuilder().setBlogId(blogId).build()
//    );
//    System.out.println("Deleted blog:" + deleteBlogResponse.getBlogId());

//    ReadBlogRequest readRequestDelete = ReadBlogRequest.newBuilder()
//      .setBlogId(blogId)
//      .build();
//    ReadBlogResponse readResponseDeleted = blogClient.readBlog(readRequestDelete);
//    System.out.println("Received read blog response: " + readResponseDeleted.toString());

    blogClient.listBlog(ListBlogRequest.newBuilder().build())
      .forEachRemaining(listBlogResponse -> System.out.println(listBlogResponse.getBlog().toString()));
  }
}
