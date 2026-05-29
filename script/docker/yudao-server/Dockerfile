# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /workspace
COPY . .
ARG MAVEN_BUILD_ARGS=""
RUN --mount=type=cache,id=xiaokanhui-maven-repository,target=/root/.m2/repository,sharing=locked \
    mvn -B -ntp -pl yudao-server -am -DskipTests -Dmaven.test.skip=true ${MAVEN_BUILD_ARGS} package

FROM eclipse-temurin:21-jre

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /yudao-server

WORKDIR /yudao-server
COPY --from=builder /workspace/yudao-server/target/yudao-server.jar app.jar

ENV TZ=Asia/Shanghai
ENV JAVA_OPTS="-Xms512m -Xmx512m -Djava.security.egd=file:/dev/./urandom -Duser.timezone=Asia/Shanghai"
ENV ARGS=""

EXPOSE 48080

CMD ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar ${ARGS}"]
