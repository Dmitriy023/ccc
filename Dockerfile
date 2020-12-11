FROM openjdk:8-jdk-alpine

ENV API_SERVER_PORT=8080
ENV MANAGEMENT_SERVER_PORT=8081
ENV JAVA_OPTS=""

EXPOSE 8080 8081

USER 1001

COPY build/libs/*.jar app.jar

ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:MaxRAMFraction=1", "-XX:MaxRAM=750M", "-jar", "app.jar"]
