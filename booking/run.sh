# docker run --net=host --rm --name booking-primary booking-service -Dexec.args=8081 &
# docker run --net=host --rm --name booking-secondary1 booking-service -Dexec.args=8083 &
# docker run --net=host --rm --name booking-secondary2 booking-service -Dexec.args=8084  &
mvn clean
mvn compile
mvn exec:java -Dexec.mainClass="com.example.App" -Dexec.args=8081 &
mvn exec:java -Dexec.mainClass="com.example.App" -Dexec.args=8083 &
mvn exec:java -Dexec.mainClass="com.example.App" -Dexec.args=8084 &
docker run -p 8080:8080 --rm --name user --add-host=host.docker.internal:host-gateway user-service &
docker run -p 8082:8080 --rm --name wallet --add-host=host.docker.internal:host-gateway wallet-service &
