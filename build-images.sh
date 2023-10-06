mvn clean install

mv target/seasonsforce-ms-company-api-1.0-SNAPSHOT.jar api-image/seasonsforce-ms-company-api-1.0-SNAPSHOT.jar

cd api-image

docker build -t company-api .

cd ../postgres-image

docker build -t company-db .