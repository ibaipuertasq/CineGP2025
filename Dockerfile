FROM maven:3.9.6-eclipse-temurin-17
WORKDIR /app
COPY . .
EXPOSE 8080
CMD sed -i "s|\${DB_URL}|$DB_URL|g" /app/src/main/resources/datanucleus.properties && mvn jetty:run