FROM openjdk:17-jdk-slim
RUN apt-get update && apt-get install -y maven
WORKDIR /app
COPY . ${WORKDIR}
EXPOSE 8080
RUN mvn compile
CMD ["sh", "-c", "mvn exec:java"]