FROM eclipse-temurin:25-jdk-noble

# Build dependencies

RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Build Server

ADD . / ./
RUN mvn -U clean install
RUN cd BankingAppServer && mvn clean compile assembly:single

# Set Entrypoint

ENTRYPOINT java -jar BankingAppServer/target/SimplyTest-BankingServer.jar

EXPOSE 5005
