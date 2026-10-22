FROM tomcat:9.0-jdk11-temurin

# Remove the default Tomcat applications
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the Maven-generated WAR into Tomcat
COPY target/flipkart-clone.war /usr/local/tomcat/webapps/ROOT.war

# Tomcat HTTP port
EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"]
