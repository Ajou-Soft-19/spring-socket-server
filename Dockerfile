FROM amazoncorretto:17
LABEL maintainer="jsm5315@ajou.ac.kr"

ARG JAR_FILE=build/libs/spring-0.0.1-SNAPSHOT.jar

WORKDIR /home/java/socket

COPY ${JAR_FILE} /home/java/socket/socket-server.jar

EXPOSE 7002

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker","/home/java/socket/socket-server.jar"]