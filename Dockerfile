FROM maven:3-eclipse-temurin-21

# Build Server

ADD . / ./
RUN mvn -U clean install
RUN cd BankingAppServer && mvn clean compile assembly:single

# Set Entrypoint

ENTRYPOINT java -jar BankingAppServer/target/SimplyTest-BankingServer.jar

EXPOSE 5005
