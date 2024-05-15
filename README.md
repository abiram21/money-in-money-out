Real-time Transaction APP using Event sourcing
===============================
## Details
These serves realtime synchronous calculation of balance on request.
This is implemented in concept of event sourcing.

The service accepts two types of transactions:
1) Load: Add money to a user (credit)

2) Authorize: Conditionally remove money from a user (debit)

Every load or authorization PUT should return the updated balance following the transaction. 
Authorization declines will be saved, even if they do not impact balance calculation.

## Bootstrap instructions
To run this server locally,

1. Install Java 17 on your local machine - https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html.
2. Install Maven on your local machine - https://maven.apache.org/install.html.
3. Clone the repository using **git clone https://github.com/codescreen/CodeScreen_szse1bjl.git**.
4. Navigate to project root directory and open a terminal.
5. Run **mvn clean package** which will run the test and build the project.
6. Run **java -jar target/CodeScreen_szse1bjl-1.0.0.jar** to start the application.
7. To test the APIs in swagger navigate to http://localhost:8080/docs.
8. Alternatively you can use an API client like postman and test the endpoints.

To run this server locally using docker,

1. Install docker on your machine.
2. In the terminal, navigate to your project root directory.
3. Run **docker build .**
4. Once its built, it will return the container id and 
5. Copy the container id or to view all the containers and get the container id run **docker ps -a**
6. Run **docker run -it -p 8080:8080 <container_id>**.
7. To stop the docker container run **docker stop <container_id>**.
8. To restart the docker container run **docker start <container_id>**.

## Assumptions
1. For a particular userId, the currency is the same for all transactions.
2. As using in-memory database, that the size of the data stored in memory will be limited by the available memory resources of the system.
3. MessageId in the path and the body should be matched otherwise error will be thrown.
4. User inserted in the system with userId **"2226e2f9-ih09-46a8-958f-d659880asdfD"** (For testing you can use this userId).

## API endpoints
1. http://localhost:8080/api/v1/ping
2. http://localhost:8080/api/v1/transaction/load/{messageId} 
3. http://localhost:8080/api/v1/transaction/authorize/{messageId}

## Design considerations
1. I chose Springboot because,
    1. It is a popular framework and provides many features like embedded server, Spring MVC, Spring Data REST, Validation, Security, etc. 
    2. It makes the application simpler (Auto configuration) and accelerates to develop quickly.
    3. It has a great community support.
2. I chose H2 In-memory database because,
    1. Since the project does not require a persistent storage.
    2. It is a SQL database which runs without external dependencies.
3. I chose Asynchronous and Retry pattern because,
    1. As the project is based on event sourcing, @Async annotation is used to achieve the processing of background task.
    2. Implemented retry to the background process for some no.of times on failures. This is achieved by using @Retryable annotation.
4. Schema Validation
    1. All the schemas provided in the open api specification are validated using @Valid annotations
    2. All the incoming requests are validated properly.
5. Unit and Integration test (99% code coverage)
    1. Implemented comprehensive unit tests using JUnit and Mockito to validate the functionality of individual components.
    2. Implemented integration test (API test) to ensure different components works together as expected.

## Future enhancements and Deployment considerations
1. CI/CD pipeline: Set up a pipeline to automate the build, test and deploy to ensure changes are deployed safely. This can be done using Github actions or AWS Code Pipeline or Jenkins.
2. Docker containerization: Create a docker image which can be deployed easily across different environments like QA, Stage and Prod(No need to setup separately and docker image is provided). If there are multiple services needed to be run I would use docker-compose.
3. Multiple instances: Deploy the application in multiple instances to increase the availability.
4. Load balancer: Create a load balancer to distribute the traffic across the instances.
5. Security: Implement SSL/TLS encryption and authentication & authorization using JWT.
6. Monitoring and logging: Configure logging and monitoring tool like Datadog. Also, I will create monitors to notify when the background job fails.
7. Serverless: Set up AWS lambda service to run the background job to minimize the error.
