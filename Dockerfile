# 第一阶段：在 Docker 提供的 Maven + JDK 17 环境中打包项目。
# 因此运行本项目的电脑不需要单独安装 JDK 和 Maven。
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# 复制项目描述文件和源码，并生成可执行的 Spring Boot JAR。
# Maven 会仅下载本项目实际需要的依赖。
COPY pom.xml ./
COPY src ./src
RUN mvn -B clean package -DskipTests

# 第二阶段：只保留运行 JAR 所需的 JRE，减小最终镜像体积。
FROM amazoncorretto:17-alpine3.24

WORKDIR /app

COPY --from=build /build/target/login-demo-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
