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

    /**
     * Upload a file to Minio.
     *
     * @param bucketName: The name of the bucket.
     * @param objectName: The name of the object.
     * @param multipartFile: The file to upload.
     * @throws IOException: If an I/O error occurs.
     * @throws NoSuchAlgorithmException: If the algorithm SHA-256 is not available.
     * @throws InvalidKeyException: If the key is invalid.
     */
    public void uploadFile(String bucketName, String objectName, MultipartFile multipartFile)
            throws IOException, NoSuchAlgorithmException, InvalidKeyException, MinioException {
        try {
            // Create a minioClient with the MinIO server, its access key and secret key.
            MinioClient minioClient = MinioClient.builder()
                    .endpoint("http://company-minio:9000")
                    .credentials("company", "companycompany")
                    .region("europe")
                    .build();

            // Verify if the bucket already exists.
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

            if (!found) {

                // Create a new bucket.
                minioClient.makeBucket(
                        MakeBucketArgs
                                .builder()
                                .bucket(bucketName)
                                .region("europe")
                                .build()
                );

                // Define the bucket policy.
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

                // Setting the bucket policy.
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs
                                .builder()
                                .bucket(bucketName)
                                .config(config)
                                .region("europe")
                                .build()
                );
            }

            // Get the input stream.
            InputStream fileInputStream = multipartFile.getInputStream();

            // Upload the file to the bucket with putObject.
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .contentType(multipartFile.getContentType()) // Définissez le type de contenu si nécessaire.
                            .stream(fileInputStream, fileInputStream.available(), -1)
                            .build());

            // Close the file stream.
            fileInputStream.close();
        } catch (InvalidKeyException e) {
            throw new InvalidKeyException("The key is invalid.");
        } catch (NoSuchAlgorithmException e) {
            throw new NoSuchAlgorithmException("The SHA-256 algorithm is not available.");
        } catch (IOException e) {
            throw new IOException("An I/O error occurs.");
        } catch (MinioException e) {
            throw new MinioException("An error occurred: " + e.getMessage());
        }
    }

    // TODO: try to make this work.
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
