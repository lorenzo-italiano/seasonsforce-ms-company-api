package fr.polytech.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


/**
 * Service to interact with Minio.
 */
@Service
public class MinioService {

    private final Logger logger = LoggerFactory.getLogger(MinioService.class);

    private final String MINIO_REGION = Optional.ofNullable(System.getenv("MINIO_REGION")).orElse("europe");

    // Initialize minioClient with MinIO server.
    private final MinioClient minioClient = MinioClient.builder()
                                                .endpoint(System.getenv("MINIO_REAL_ENDPOINT"))
                                                .credentials(System.getenv("MINIO_USERNAME"), System.getenv("MINIO_PASSWORD"))
                                                .region(MINIO_REGION)
                                                .build();

    /**
     * Create a public bucket in Minio.
     *
     * @param bucketName: The name of the bucket.
     * @throws MinioException if an error occurs.
     * @throws IOException if an I/O error occurs.
     * @throws NoSuchAlgorithmException if an algorithm is not available.
     * @throws InvalidKeyException if the key is invalid.
     */
    private void createPublicBucket(String bucketName) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {

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

        createBucket(bucketName, config);
    }

    /**
     * Create a private bucket in Minio.
     *
     * @param bucketName: The name of the bucket.
     * @throws MinioException if an error occurs.
     * @throws IOException if an I/O error occurs.
     * @throws NoSuchAlgorithmException if an algorithm is not available.
     * @throws InvalidKeyException if the key is invalid.
     */
    private void createPrivateBucket(String bucketName) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {

        String config = "{\n" +
                "    \"Statement\": [\n" +
                "        {\n" +
                "            \"Action\": [\n" +
                "                \"s3:GetBucketLocation\",\n" +
                "                \"s3:ListBucket\"\n" +
                "            ],\n" +
                "            \"Effect\": \"Allow\",\n" +
                "            \"Principal\": {\n" +
                "                \"AWS\": \"arn:aws:iam::company:root\"\n" +
                "            },\n" +
                "            \"Resource\": \"arn:aws:s3:::" + bucketName + "\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"Action\": \"s3:GetObject\",\n" +
                "            \"Effect\": \"Allow\",\n" +
                "            \"Principal\": {\n" +
                "                \"AWS\": \"arn:aws:iam::company:root\"\n" +
                "            },\n" +
                "            \"Resource\": \"arn:aws:s3:::" + bucketName + "/*\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"Version\": \"2012-10-17\"\n" +
                "}";

        createBucket(bucketName, config);
    }


    /**
     * Create a bucket in Minio.
     *
     * @param bucketName: The name of the bucket.
     * @param config: The configuration of the bucket.
     * @throws MinioException if an error occurs.
     * @throws IOException if an I/O error occurs.
     * @throws NoSuchAlgorithmException if an algorithm is not available.
     * @throws InvalidKeyException if the key is invalid.
     */
    private void createBucket(String bucketName, String config) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {

        // Create a new bucket.
        minioClient.makeBucket(
                MakeBucketArgs
                        .builder()
                        .bucket(bucketName)
                        .region(MINIO_REGION)
                        .build()
        );

        // Setting the bucket policy.
        minioClient.setBucketPolicy(
                SetBucketPolicyArgs
                        .builder()
                        .bucket(bucketName)
                        .config(config)
                        .region(MINIO_REGION)
                        .build()
        );
    }


    /**
     * Check if a bucket exists.
     *
     * @param bucketName: The name of the bucket.
     * @return True if the bucket exists, false otherwise.
     * @throws MinioException if an error occurs.
     * @throws IOException if an I/O error occurs.
     * @throws NoSuchAlgorithmException if an algorithm is not available.
     * @throws InvalidKeyException if the key is invalid.
     */
    private boolean bucketExists(String bucketName) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }

    /**
     * Create a bucket if it does not exist.
     *
     * @param bucketName: The name of the bucket.
     * @param isPublic: True if the bucket should be public, false otherwise.
     * @throws MinioException if an error occurs.
     * @throws IOException if an I/O error occurs.
     * @throws NoSuchAlgorithmException if an algorithm is not available.
     * @throws InvalidKeyException if the key is invalid.
     */
    private void createBucketIfNotExists(String bucketName, boolean isPublic) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        if (!bucketExists(bucketName)) {
            if (isPublic) {
                createPublicBucket(bucketName);
            } else {
                createPrivateBucket(bucketName);
            }
        }
    }

    /**
     * Upload a file to Minio.
     *
     * @param bucketName: The name of the bucket.
     * @param objectName: The name of the object.
     * @param multipartFile: The file to upload.
     * @throws IOException If an I/O error occurs.
     * @throws NoSuchAlgorithmException If the algorithm SHA-256 is not available.
     * @throws InvalidKeyException If the key is invalid.
     */
    public void uploadFile(String bucketName, String objectName, MultipartFile multipartFile, boolean isPublicFile) throws IOException, NoSuchAlgorithmException, InvalidKeyException, MinioException {
        logger.info("Starting the upload of a file to Minio");

        createBucketIfNotExists(bucketName, isPublicFile);

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

        logger.info("Completed the upload of a file to Minio");
    }

    /**
     * Get the private URL of an object in Minio.
     *
     * @param bucket: The name of the bucket.
     * @param object: The name of the object.
     * @return The private URL of the object.
     */
    public String getPrivateDocumentUrl(String bucket, String object) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {

        logger.info("Getting the private URL of an object in Minio with bucketName: " + bucket + " and object: " + object);
        // Générez l'URL de l'objet dans Minio.
        String url = null;
//        Map<String, String> reqParams = new HashMap<String, String>();
//        reqParams.put("response-content-type", "application/json");

            url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .region(MINIO_REGION)
                            .bucket(bucket)
                            .object(object)
//                            .extraHeaders()
                            .expiry(2, TimeUnit.HOURS)
                            .build());

        logger.info("Completed getting the private URL of an object in Minio");

        return url;
    }

    /**
     * Delete a file from a bucket.
     *
     * @param bucketName: The name of the bucket.
     * @param objectName: The name of the object.
     * @throws MinioException if an error occurs.
     * @throws IOException if an I/O error occurs.
     * @throws NoSuchAlgorithmException if an algorithm is not available.
     * @throws InvalidKeyException if the key is invalid.
     */
    public void deleteFileFromPrivateBucket(String bucketName, String objectName) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        minioClient.removeObject(
            RemoveObjectArgs
                .builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
    }

    /**
     * Check if a document exists and belongs to a company.
     * @param companyId Company id.
     * @param objectName Object name.
     * @return True if the document exists and belongs to the company, false otherwise.
     * @throws HttpClientErrorException If the document microservice returns an error.
     */
    public boolean documentExistsAndBelongsToCompany(UUID companyId, String objectName) throws HttpClientErrorException {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(companyId.toString())
                            .object(objectName)
                            .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
