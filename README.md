# azustor

## What's this?

This is a minimalistic object storage which can either be used as a library, or as a RESTful service.

It was built with a very set of very specific requirements:
- plays nice with backing up to DVDs
- plays nice with Docker
- has a permissive license
- can work with small memory footprint and can therefore work nicely in 32-bit environments
- is portable
- has portable volume files

## How does it work?
An Azustore instance (called "bucket" in the code) only knows two operations:
- Get object via UUID
- Store object

A bucket consists of a control-file, and one or more volume files. 
The control file is created by Azustor during initalization. 
A new volume file will be created when none exist, or when all the existing ones are growing too large.

When storing an object Azustor will assign the object an UUId and append the object and its corresponding uuid to a volume 
file, or create a new volume file when the existing ones are becoming too large.

Azustor knows of two operating modes:
- Low Memory Mode: When an object is requested, Azustor scans all it's volume files and extract the object when found. This might become slow when you're dealing with a lot of small files.
- High Memory Mode: Azustor scans all volumes once on start, keeps an index in memory. A high number of files does not cause a big performance penalty when this is used.

## Using Azustor as a Java Library
1. Run `mvn clean package install` in the azustor4j directory.
2. Add Azustor to your project by specifying the following dependency in your POM:

`
<dependency>
<groupId>moe.yo3explorer</groupId>
<artifactId>azustor4j</artifactId>
<version>1.0.3-SNAPSHOT</version>
</dependency>
`

3. Somewhere in your project use `AzustorBucket.createBucket` or `AzustorBucket.loadBucket` to initialize Azustore.
4. Use the storeFile Method to store objects, or the retrieveFile to fetch objects.

## Using Azustor as a RESTful service

1. Run `mvn clean package install` in the azustor4j directory to build and install the dependencies.
2. Run `mvn clean package` in the azustor4rest directory to build the RESTful server.
3. In azustor4rest/target/quarkus-app do `java -jar quarkus-run.jar` to run the RESTful srvice. It listens on port 8080 by default.

Upload objects using `curl -v --data-binary @2021_0109_2255_27177.bmp http://127.0.0.1:8080` 
Pay attention to the "Location" Header in the response. It gives you the URL in which you find the object.
If you do not need the URL and are happy with just the UUID, it's in the "X-Azustor-UUID" header.

Download objects using `curl -v -o test.bmp http://127.0.0.1:8080/bc6997b5-9eb0-4883-9228-244b536e3ea3`

## Using Azustor's RESTful service using Microprofile REST client

In azustor4rest/test/java/moe/yo3explorer/azustor/client/AzustorClient.java you can find a pre-written Microprofile 
@RegisterRestClient which can communicate with Azustor's RESTful service.

To use it in Quarkus, you can put the following in your application.properties:

`azustor-client/mp-rest/url=http://127.0.0.1:8081/`

`azustor-client/mp-rest/scope=javax.inject.Singleton`

# Comparing Azustor to other object storages:

There are excellent object storage solutions out there, I usually recommend Minio or SeaweedFS to people. If you're 
looking for object storage, you should check out these first before considering Azustor, since Azustore was designed 
with very specific requirements in mind.

## Comparing Azustor with Minio

Minio is fully S3 compatible, while Azustor is not. If you require S3 compatibility, please consider using Minio. 


## Comparing Azustor with SeaweedFS

SeaweedFS scales extremely well across multiple machines. Azustor has no concept of clusters at all.
If you need clustering, scaling, replication, please consider using SeaweedFS.

# So, why did you write Azustor?

Why not? None of the other object storages I found quite fit a specific use-case I have. 
I needed to store a lot of picture files in a database without using BLOBs. 
Also, I need to sync the database to a portable device for offline usage. Azustor can accomplish exactly that. 
When I'm at home, I can put stuff into the database, and when I need it on my tablet on the go, I copy Azustor's bucket 
and an SQL-Dump of my database onto the portable device. Works great!
