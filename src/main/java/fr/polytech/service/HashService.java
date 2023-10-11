package fr.polytech.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class HashService {

    public String hash(String stringToHash) {

        try {
            // Créez un objet MessageDigest avec l'algorithme SHA-512
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Convertissez la chaîne en tableau d'octets (bytes)
            byte[] bytes = stringToHash.getBytes(StandardCharsets.UTF_8);

            // Mettez les données à hacher dans le MessageDigest
            md.update(bytes);

            // Effectuez le hachage
            byte[] hash = md.digest();

            // Convertissez le tableau d'octets en une représentation hexadécimale
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }

            // Tronquez les deux derniers caractères
            String truncatedHash = hexString.substring(0, 62);

            return truncatedHash;

//            System.out.println("Chaîne d'origine: " + input);
//            System.out.println("SHA-512 Hash: " + hexString.toString());

        } catch (NoSuchAlgorithmException e) {
            System.err.println("L'algorithme SHA-512 n'est pas disponible.");
        } catch (Exception e) {
            System.err.println("Une erreur s'est produite : " + e.getMessage());
        }

        return "";
    }


}
