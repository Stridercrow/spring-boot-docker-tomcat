FROM tomcat:8.5-jdk11-openjdk-slim
ADD target/sample.war /usr/local/tomcat/webapps/
RUN apt-get update && \
    apt-get install -y libreoffice && \
    rm -rf /var/lib/apt/lists/*
EXPOSE 8080
CMD ["catalina.sh", "run"]