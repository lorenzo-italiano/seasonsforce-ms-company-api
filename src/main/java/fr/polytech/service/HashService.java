package fr.polytech.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class HashService {

    /**
     * Hashes a string using the SHA-256 algorithm.
     *
     * @param stringToHash The string to hash.
     * @return The hashed string.
     */
    public String hash(String stringToHash) throws NoSuchAlgorithmException {

        try {
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

            return truncatedHash;

        } catch (NoSuchAlgorithmException e) {
            throw new NoSuchAlgorithmException("The SHA-256 algorithm is not available.");
        }
    }

}
