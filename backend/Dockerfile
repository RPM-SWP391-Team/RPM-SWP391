FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]FROM node:20-alpine AS build
                                      WORKDIR /app
                                      COPY package*.json ./
                                      RUN npm install
                                      COPY . .
                                      RUN npm run build

                                      FROM nginx:alpine
                                      COPY --from=build /app/dist /usr/share/nginx/html
                                      EXPOSE 80