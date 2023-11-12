FROM fedora:38

# Build dependencies

RUN dnf install -y java-17-openjdk maven

# Build Server

ADD . / ./
RUN mvn -U clean install
RUN cd BankingAppServer && mvn clean compile assembly:single

# Set Entrypoint

ENTRYPOINT java -jar BankingAppServer/target/SimplyTest-BankingServer.jar
