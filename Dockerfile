FROM eclipse-temurin:17-jre
WORKDIR /app
# 将本地打包的可执行 JAR 放入镜像
COPY target/*.jar app.jar
ENV JAVA_OPTS="-Xms256m -Xmx512m"
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]