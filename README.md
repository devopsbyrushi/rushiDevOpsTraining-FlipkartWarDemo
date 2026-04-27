# Flipkart Clone (WAR Version) - by Rushi

Web application demo for Maven Day 02 (deploy on Apache Tomcat).

## Build & Deploy

```bash
mvn clean package
sudo cp target/flipkart-clone.war /var/lib/tomcat9/webapps/
sudo systemctl restart tomcat9
```

Open browser: **http://localhost:8080/flipkart-clone/**
