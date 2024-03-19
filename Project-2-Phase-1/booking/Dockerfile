FROM openjdk:17-jdk-slim
RUN apt-get install maven
WORKDIR /app
COPY . ${WORKDIR}
EXPOSE 8080
CMD ["sh", "-c", "mvn exec:java"]