FROM openjdk:17
WORKDIR /app
COPY . ${WORKDIR}
RUN ./mvnw package -Ptest
CMD ["sh", "-c", "java -jar ./target/*.jar --spring.profiles.active=production --debug"]