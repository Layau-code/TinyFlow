FROM --platform=linux/amd64 docker.m.daocloud.io/maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace

COPY docker/maven-settings.xml /tmp/maven-settings.xml
COPY pom.xml ./

COPY src ./src
COPY web ./web
RUN --mount=type=cache,target=/root/.m2/repository mvn -s /tmp/maven-settings.xml -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar /app/app.jar
ENV JAVA_OPTS="-Xms256m -Xmx512m"
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
