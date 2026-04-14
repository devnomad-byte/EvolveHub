# ==============================================================
# EvolveHub Backend - Lightweight Runtime Dockerfile
# ==============================================================
# 预构建 JAR 后使用：
#   mvn clean package -DskipTests
#   docker build --build-arg MODULE=evolvehub-auth -t evolvehub-auth .
# ==============================================================

FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="EvolveHub Team"

RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

WORKDIR /app

ARG MODULE
COPY ${MODULE}/target/*.jar app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
