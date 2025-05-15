FROM maven:3.9.6-eclipse-temurin-17
WORKDIR /app
COPY . .
EXPOSE 8080
CMD ["mvn", "jetty:run"]