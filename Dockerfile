FROM maven:3.8.3-openjdk-17 AS build

WORKDIR /transaction-app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "target/transaction-0.0.1-SNAPSHOT.jar"]