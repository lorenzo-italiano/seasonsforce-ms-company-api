package fr.polytech.service;

import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Service
public class MinioService {

    public void uploadFile(String bucketName, String objectName, MultipartFile multipartFile)
            throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        try {
            System.out.println("dans uploadFile");

            // Créez un MinioClient avec les informations de configuration.
            MinioClient minioClient = MinioClient.builder()
                    .endpoint("http://company-minio:9000")
                    .credentials("company", "companycompany")
                    .region("europe")
                    .build();

            System.out.println("après minioClient");

            // Vérifiez si le bucket existe, sinon créez-le.
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

            System.out.println("avant if");
            if (!found) {

                System.out.println("dans if");
                minioClient.makeBucket(
                        MakeBucketArgs
                                .builder()
                                .bucket(bucketName)
                                .region("europe")
                                .build()
                );

                System.out.println("après makeBucket");

                // Définissez la politique du bucket pour rendre tous les objets lus uniquement par défaut.
                String config = "{\n" +
                        "    \"Statement\": [\n" +
                        "        {\n" +
                        "            \"Action\": [\n" +
                        "                \"s3:GetBucketLocation\",\n" +
                        "                \"s3:ListBucket\"\n" +
                        "            ],\n" +
                        "            \"Effect\": \"Allow\",\n" +
                        "            \"Principal\": \"*\",\n" +
                        "            \"Resource\": \"arn:aws:s3:::" + bucketName + "\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"Action\": \"s3:GetObject\",\n" +
                        "            \"Effect\": \"Allow\",\n" +
                        "            \"Principal\": \"*\",\n" +
                        "            \"Resource\": \"arn:aws:s3:::" + bucketName + "/*\"\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"Version\": \"2012-10-17\"\n" +
                        "}";

                System.out.println("après config");

                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs
                                .builder()
                                .bucket(bucketName)
                                .config(config)
                                .region("europe")
                                .build()
                );
            }

            System.out.println("après if");

            // Récupérez le flux d'entrée du fichier MultipartFile.
            InputStream fileInputStream = multipartFile.getInputStream();

            System.out.println("après fileInputStream");

            // Téléchargez l'objet vers Minio en utilisant putObject.
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .contentType(multipartFile.getContentType()) // Définissez le type de contenu si nécessaire.
                            .stream(fileInputStream, fileInputStream.available(), -1)
                            .build());

            System.out.println("après putObject");

            System.out.println("Fichier téléchargé avec succès dans Minio.");
        } catch (MinioException e) {
            System.out.println("Une erreur s'est produite : " + e);
            System.out.println("Trace HTTP : " + e.httpTrace());
        } catch (Exception e) {
            System.out.println("Une erreur s'est produite : " + e);
        }
    }

    public String getPublicImageUrl(String bucket, String object) {
        try {

            MinioClient minioClient =
                    MinioClient.builder()
                            .endpoint("http://company-minio", 9000, false)
//                            .credentials("qlGIWz9mvnK8FaLB3KFp", "m1iaG4TY4eYSHkrF8QVmeIdYKsJ4XK5GQVlOA3Rd")
                            .credentials("company", "companycompany")
                            .build();

            // Générez l'URL de l'objet dans Minio.
            String url = null;
            Map<String, String> reqParams = new HashMap<String, String>();
            reqParams.put("response-content-type", "application/json");
            try {

                url = minioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .method(Method.GET)
                                .region("europe")
                                .bucket(bucket)
                                .object(object)
                                .expiry(2, TimeUnit.HOURS)
//                                .extraQueryParams(reqParams)
                                .build());

            } catch (InvalidKeyException e) {
                System.out.println("error while getting presigned url");
                throw new RuntimeException(e);
            } catch (IOException e) {
                System.out.println("error while getting presigned url");
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                System.out.println("error while getting presigned url");
                throw new RuntimeException(e);
            }

            return url;
        } catch (MinioException e) {
            // Gérez les erreurs ici.
            return null;
        }
    }
}
