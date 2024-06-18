FROM openjdk:17-jdk-slim
WORKDIR /app
COPY . ${WORKDIR}
ENV WALLET_SERVICE_HOST="wallet-service"
ENV WALLET_SERVICE_PORT="8082"
ENV BOOKING_SERVICE_HOST="booking-service"
ENV BOOKING_SERVICE_PORT="8081"
ENV USER_SERVICE_HOST="user-service"
ENV USER_SERVICE_PORT="8080"
EXPOSE 8080
RUN ./mvnw package -Ptest
CMD ["sh", "-c", "java -jar ./target/*.jar --spring.profiles.active=production --debug"]
