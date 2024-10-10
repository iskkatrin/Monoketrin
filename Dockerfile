# Используем официальный образ OpenJDK
FROM openjdk:17-jdk-slim

# Устанавливаем Maven
RUN apt-get update && apt-get install -y maven

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файл pom.xml и загружаем зависимости
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем весь код приложения
COPY src ./src

# Собираем приложение
RUN mvn clean package -DskipTests

# Указываем команду для запуска приложения
CMD ["java", "-jar", "target/Bot-Monoketrin-0.0.1-SNAPSHOT.jar"]