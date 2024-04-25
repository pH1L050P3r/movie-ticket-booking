FROM openjdk:17-jdk-slim
RUN apt-get update && apt-get install -y maven
WORKDIR /app
COPY . ${WORKDIR}
EXPOSE 22593
EXPOSE 8080
EXPOSE 8083
EXPOSE 8084
RUN mvn compile
ENTRYPOINT ["mvn", "exec:java", "-Dexec.mainClass=com.example.App"]
# CMD ["sh", "-c", "mvn exec:java", "-Dexec.mainClass=fully.qualified.main.class.name"]