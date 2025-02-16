FROM gradle:8.12.1-jdk21-graal AS build
WORKDIR /project
COPY src ./src
COPY build.gradle.kts .
COPY settings.gradle.kts .
RUN gradle clean build && gradle nativeCompile

FROM container-registry.oracle.com/os/oraclelinux:9-slim
RUN groupadd graalvm && useradd -r -g graalvm app_user
COPY --from=build --chown=app_user:graalvm /project/build/native/nativeCompile/my_app app
EXPOSE 8080
USER app_user
ENTRYPOINT ["/app", "-Xms64m", "-Xmx128m"]