package fr.polytech.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class HashService {

    private final Logger logger = LoggerFactory.getLogger(HashService.class);

    /**
     * Hashes a string using the SHA-256 algorithm.
     *
     * @param stringToHash The string to hash.
     * @return The hashed string.
     */
    public String hash(String stringToHash) throws NoSuchAlgorithmException {

        try {
            logger.info("Starting the hashing of a string");

            // Create a SHA-512 MessageDigest instance
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Convert the input string to a byte array and put it into the MessageDigest
            byte[] bytes = stringToHash.getBytes(StandardCharsets.UTF_8);

            // Perform the hashing
            md.update(bytes);

            // Retrieve the hash's bytes
            byte[] hash = md.digest();

            // Convert it to hexadecimal format
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }

            // Truncate the hash to 62 characters (the maximum length of a bucket name)
            String truncatedHash = hexString.substring(0, 62);

            logger.info("The hashing of a string has been completed");

            return truncatedHash;

        } catch (NoSuchAlgorithmException e) {
            logger.error("The hashing of a string has failed: the SHA-256 algorithm is not available");
            throw new NoSuchAlgorithmException("The SHA-256 algorithm is not available.");
        }
    }

}
