FROM openjdk:13-slim
WORKDIR /app/
ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n
COPY target/transaction-0.0.1.jar /app/transaction.jar
COPY src/main/resources/ca.crt /app/
COPY src/main/resources/client.transaction_service_api.crt /app/
COPY src/main/resources/client.transaction_service_api.key.pk8 /app/
ENTRYPOINT ["java", "-jar", "transaction.jar"]
EXPOSE 8000/tcp
EXPOSE 8900/tcp