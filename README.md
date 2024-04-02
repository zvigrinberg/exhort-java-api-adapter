# exhort-java-api-adapter
java adapter to run exhort-java-api image scanning capabilities.

## Procedure

1. Replace username and password tags' values in settings.xml to your github user and an authorized `personal access token`

2. install the exhort-java-api-adapter
  ```shell
  mvn -s ./settings.xml clean package
  ```
3. Sample Invocation
```shell
java -jar target/exhort-java-api-adapter-1.0-SNAPSHOT-jar-with-dependencies.jar json quay.io/zgrinber/wiremock:latest^^linux/amd64 quay.io/zgrinber/installer:4.9
``` 
