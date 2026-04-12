FROM maven:3-eclipse-temurin-21

# Build Server

ADD . / ./
RUN mvn -U clean install

# Set Entrypoint

WORKDIR BankingAppServer
ENTRYPOINT mvn spring-boot:run 

EXPOSE 5005
