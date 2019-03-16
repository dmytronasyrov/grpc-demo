package com.pharosproduction.grpc.blog.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
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
      Blog blog = Blog.newBuilder()
        .setAuthorId(document.getString("author_id"))
        .setTitle(document.getString("title"))
        .setContent(document.getString("content"))
        .setId(blogId)
        .build();

      responseObserver.onNext(ReadBlogResponse.newBuilder().setBlog(blog).build());
      responseObserver.onCompleted();
    }
  }
}
