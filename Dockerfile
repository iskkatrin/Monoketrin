# Используем официальный образ OpenJDK с JDK 17
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию в контейнере
WORKDIR /app

# Копируем файл сборки проекта (JAR-файл) в контейнер
COPY ./target/Bot-Monoketrin-0.0.1-SNAPSHOT.jar app.jar

# Указываем команду для запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]