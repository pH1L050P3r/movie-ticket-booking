FROM openjdk:17
WORKDIR /app
COPY . ${WORKDIR}
RUN ./mvnw package
CMD ["sh", "-c", "java -jar ./target/*.jar"]