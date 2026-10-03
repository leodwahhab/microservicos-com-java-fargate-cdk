# Imagem base: contem tudo que a aplicacao precisa para executar, incluindo o Java.
# A aula usa "openjdk:15"; aqui usamos Java 17 porque este projeto e Spring Boot 4 / Java 17.
FROM eclipse-temurin:17-jre

VOLUME /tmp

# Caminho do jar, injetado pelo dockerfile-maven-plugin (buildArgs/JAR_FILE no pom.xml).
ARG JAR_FILE
COPY ${JAR_FILE} app.jar

# Ponto de entrada: classe principal da aplicacao
# (com.example.aws_project01.AwsProject01Application, definida como Start-Class
# no manifesto do jar pelo spring-boot-maven-plugin).
ENTRYPOINT ["java", "-jar", "/app.jar"]
