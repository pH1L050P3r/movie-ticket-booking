FROM openjdk:17
WORKDIR /app
COPY . ${WORKDIR}
ENV WALLET_SERVICE_HOST host.docker.internal
ENV WALLET_SERVICE_PORT 8083
ENV BOOKING_SERVICE_HOST host.docker.internal
ENV BOOKING_SERVICE_PORT 8082
ENV USER_SERVICE_HOST host.docker.internal
ENV USER_SERVICE_PORT 8081
RUN ./mvnw package
EXPOSE 8080
CMD ["sh", "-c", "java -jar ./target/*.jar --debug"]