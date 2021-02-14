FROM openjdk:8-jdk-alpine

#RUN mvn clean compile assembly:single

COPY target/seth-1.3.2-Snapshot-jar-with-dependencies.jar /app/seth.jar
COPY resources/mutations.txt /app/resources/mutations.txt
WORKDIR /app

EXPOSE 8080
CMD ["java", "-cp", "seth.jar", "seth.seth.webservice.MessageResource"]


#docker build -t seth .
#docker run -d -p 8080:8080 --network host  seth


#####Dockerhub
#docker build -t erechtheus79/seth .
#docker login
#docker push erechtheus79/seth:latest

#docker pull erechtheus79/seth
#docker run -d -p 8080:8080 --network host  erechtheus79/seth