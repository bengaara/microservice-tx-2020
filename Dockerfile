#FROM maven:latest AS build
#COPY src /usr/app/src
#COPY  pom.xml /usr/app
#WORKDIR /usr/app
#RUN mvn dependency:go-offline



# Step : Test and package
FROM maven:3.6.3-openjdk-14-slim as target
ARG PROFILE="uat"
RUN echo "PROFILE is $PROFILE"
ARG USER_HOME_DIR="~"
VOLUME "$USER_HOME_DIR/.m2"
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

WORKDIR /build
COPY extract.sh pom.xml /build/
RUN chmod +x /build/extract.sh
RUN mkdir target
#RUN mvn dependency:go-offline
#RUN ["chmod", "+x", "/build/extract.sh"]
#RUN mvn -B -e -C -T 1C  dependency:resolve-plugins dependency:resolve dependency:go-offline
RUN mvn -Dmaven.test.skip=true -B -e -C -T 1C  dependency:resolve-plugins dependency:resolve dependency:go-offline

COPY src/ /build/src/
RUN mvn clean install -Dspring-boot.run.profiles=sand -DskipTests -e
#RUN  /build/extract.sh
#FROM    maven:latest AS build
#
#ARG PROFILE=dev
#
#RUN     mkdir /docker
#WORKDIR /docker
#COPY    pom.xml /docker
#RUN     mvn dependency:resolve
#COPY    src /docker
##RUN mvn -f /usr/app/pom.xml  -Dspring-boot.run.profiles=prod -DskipTests clean package
#RUN     mvn -Dspring-boot.run.profiles=${PROFILE} -DskipTests clean package
#openjdk:15-alpine
#FROM  openzipkin/jre-full:14.0.1-14.28.21  as runtime
FROM  openjdk:14-alpine  as runtime
#ARG PROFILE=sand
#RUN apt-get update &&  install -y ttf-dejavu
RUN apk add ttf-dejavu
WORKDIR /app

#COPY --from=build /docker/target/transaction-0.0.1.jar /app/transaction.jar
#COPY src/main/resources/client.transaction_service_api-prod.crt /app/
#COPY src/main/resources/client.transaction_service_api.key-prod.pk8 /app/

#RUN apk update && apk upgrade \
#   && apk add --no-cache ttf-dejavu \
#   # Install windows fonts as well. Not required..
#   && apk add --no-cache msttcorefonts-installer \
#   && update-ms-fonts && fc-cache -f


#RUN groupadd -r tomcat -g 1000 && useradd -u 1000 -r -g tomcat -m -d /app -s /sbin/nologin -c "Tomcat user" tomcat && \
#    chmod 755 /app
    # &&  chmod 755 /var/run/docker.sock &&  chmod 755 /usr/bin/docker
# Specify the user which should be used to execute all commands below
#USER root



#,"--spring.profiles.active=sand"
EXPOSE 8000/tcp
EXPOSE 8900/tcp


#COPY target/transaction-0.0.1.jar src/main/resources/ /app/

#COPY target/BOOT-INF/lib /app/lib
#COPY target/META-INF /app/META-INF
#COPY target/org /app/org
###dont know why the certs are better this way-- todo remove this
##COPY target/classes /app
#COPY target/BOOT-INF/classes /app

COPY --from=target /build/target/lib /app/lib
COPY --from=target /build/target/classes /app
#COPY  target/lib /app/lib
#COPY  target/classes /app
#COPY --from=target /build/target/META-INF /app/META-INF
#COPY --from=target /build/target/org /app/org

#ENTRYPOINT ["tail", "-f", "/dev/null"]
#ENTRYPOINT ["java", "-jar","transaction-0.0.1.jar"]
ENTRYPOINT ["java","-cp",".:lib/*","net.tospay.transaction.Application"]

#USER root
#COPY  --from=build /usr/app/src/main/resources/ca-prod.crt /app/
#COPY  --from=build /usr/app/src/main/resources/client.transaction_service_api-prod.crt /app/
#COPY  --from=build /usr/app/src/main/resources/client.transaction_service_api.key-prod.pk8 /app/
#COPY  --from=build /usr/app/src/main/resources/client.transaction_service_api.key-prod.pk8 /app/
#ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n
##COPY target/transaction-0.0.1.jar /app/transaction.jar
##COPY src/main/resources/ca.crt /app/
##COPY src/main/resources/client.transaction_service_api.crt /app/
##COPY src/main/resources/client.transaction_service_api.key.pk8 /app/
#ENTRYPOINT ["java", "-jar", "transaction.jar"]
#EXPOSE 8000/tcp
#EXPOSE 8900/tcp

#FROM  openjdk:14-alpine  as skiptarget
##ARG PROFILE=sand
##RUN apt-get update &&  install -y ttf-dejavu
#RUN apk add ttf-dejavu
#WORKDIR /app
#
#EXPOSE 8000/tcp
#EXPOSE 8900/tcp
#
#COPY  target/lib /app/lib
#COPY  target/classes /app
#
#ENTRYPOINT ["java","-cp",".:lib/*","net.tospay.transaction.Application"]