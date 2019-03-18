package com.pharosproduction.grpc.blog.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.proto.blog.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

  private MongoClient mMongoClient = MongoClients.create("mongodb://localhost:27017");
  private MongoDatabase mDatabase = mMongoClient.getDatabase("mydb");
  private MongoCollection<Document> collection = mDatabase.getCollection("blog");

  @Override
  public void createBlog(CreateBlogRequest request, StreamObserver<CreateBlogResponse> responseObserver) {
    System.out.println("Received create blog request");

    Blog blog = request.getBlog();
    Document doc = new Document("author_id", blog.getAuthorId())
      .append("title", blog.getTitle())
      .append("content", blog.getContent());
    collection.insertOne(doc);

    String id = doc.getObjectId("_id").toString();
    System.out.println("Inserted blog" + id);

    CreateBlogResponse response = CreateBlogResponse.newBuilder()
      .setBlog(blog.toBuilder().setId(id).build())
      .build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void readBlog(ReadBlogRequest request, StreamObserver<ReadBlogResponse> responseObserver) {
    System.out.println("Received read blog request");

    String blogId = request.getBlogId();
    Document document = null;

    try {
      document = collection.find(eq("_id", new ObjectId(blogId)))
        .first();
    } catch (Exception e) {
      Throwable err = Status.NOT_FOUND
        .withDescription("No such ID")
        .augmentDescription(e.getMessage())
        .asRuntimeException();
      responseObserver.onError(err);
    }

    if (document == null) {
      Throwable err = Status.NOT_FOUND
        .withDescription("No such ID")
        .asRuntimeException();
      responseObserver.onError(err);
    } else {
      Blog blog = documentToBlog(document);

      responseObserver.onNext(ReadBlogResponse.newBuilder().setBlog(blog).build());
      responseObserver.onCompleted();
    }
  }

  private Blog documentToBlog(Document document) {
    return Blog.newBuilder()
      .setAuthorId(document.getString("author_id"))
      .setTitle(document.getString("title"))
      .setContent(document.getString("content"))
      .setId(document.getObjectId("_id").toString())
      .build();
  }

  @Override
  public void updateBlog(UpdateBlogRequest request, StreamObserver<UpdateBlogResponse> responseObserver) {
    System.out.println("Received update blog request");

    Blog blog = request.getBlog();
    String blogId = blog.getId();
    Document document = null;

    try {
      document = collection.find(eq("_id", new ObjectId(blogId)))
        .first();
    } catch (Exception e) {
      Throwable err = Status.NOT_FOUND
        .withDescription("No such ID")
        .augmentDescription(e.getMessage())
        .asRuntimeException();
      responseObserver.onError(err);
    }

    if (document == null) {
      Throwable err = Status.NOT_FOUND
        .withDescription("No such ID")
        .asRuntimeException();
      responseObserver.onError(err);
    } else {
      Document replacement = new Document("author_id", blog.getAuthorId())
        .append("title", blog.getTitle())
        .append("content", blog.getContent())
        .append("_id", new ObjectId(blogId));

      collection.replaceOne(eq("_id", document.getObjectId("_id")), replacement);

      responseObserver.onNext(
        UpdateBlogResponse.newBuilder()
          .setBlog(documentToBlog(replacement))
          .build()
      );

      responseObserver.onCompleted();
    }
  }

  @Override
  public void deleteBlog(DeleteBlogRequest request, StreamObserver<DeleteBlogResponse> responseObserver) {
    System.out.println("Received delete blog request");

    String blogId = request.getBlogId();
    DeleteResult result = null;

    try {
      result = collection.deleteOne(eq("_id", new ObjectId(blogId)));
    } catch (Exception e) {
      Throwable err = Status.NOT_FOUND
        .withDescription("No such ID")
        .augmentDescription(e.getMessage())
        .asRuntimeException();
      responseObserver.onError(err);
    }

    if (result.getDeletedCount() == 0) {
      Throwable err = Status.NOT_FOUND
        .withDescription("No such ID")
        .asRuntimeException();
      responseObserver.onError(err);
    } else {
      System.out.println("Blog was deleted");

      responseObserver.onNext(DeleteBlogResponse.newBuilder().setBlogId(blogId).build());
      responseObserver.onCompleted();
    }
  }

  @Override
  public void listBlog(ListBlogRequest request, StreamObserver<ListBlogResponse> responseObserver) {
    System.out.println("Received list blog request");

    collection.find().iterator().forEachRemaining(document -> responseObserver.onNext(
      ListBlogResponse.newBuilder().setBlog(documentToBlog(document)).build()
    ));
    responseObserver.onCompleted();
  }
}
