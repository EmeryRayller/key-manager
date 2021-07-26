FROM openjdk:11-jdk-slim
EXPOSE 50051
ARG JAR_FILE=build/libs/*-all.jar

ADD ${JAR_FILE} app.jar

# ENV APP_NAME key-manager
# ENV BCB_URL 192.168.2.103:8082
# ENV DB_URL jdbc:mysql://192.168.2.103:3308/desafio_pix
# ENV ITAU_URL 192.168.2.103:9091

ENV APP_NAME key-manager

ENTRYPOINT [ "java", "-jar", "/app.jar" ]