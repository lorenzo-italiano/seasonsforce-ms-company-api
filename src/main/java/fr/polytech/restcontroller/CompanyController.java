package fr.polytech.restcontroller;

import fr.polytech.model.Company;
import fr.polytech.model.CompanyDetailsDTO;
import fr.polytech.model.CompanyMinimizedDTO;
import fr.polytech.service.CompanyService;
import fr.polytech.service.HashService;
import fr.polytech.service.MinioService;
import io.minio.errors.MinioException;
import jakarta.ws.rs.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/company")
public class CompanyController {

    private final Logger logger = LoggerFactory.getLogger(CompanyController.class);

    @Autowired
    private CompanyService companyService;

    @Autowired
    private MinioService minioService;

    @Autowired
    private HashService hashService;

    /**
     * Get all companies.
     *
     * @return List of all companies.
     */
    @GetMapping("/")
    public ResponseEntity<List<Company>> getAllCompanies() {
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    /**
     * Get company by id.
     *
     * @param id Company id.
     * @return Company with the specified id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompanyById(@PathVariable("id") UUID id) {
        try {
            return ResponseEntity.ok(companyService.getCompanyById(id));
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get company by id with all its details.
     *
     * @param id Company id.
     * @return Company with the specified id and all its details.
     */
    @GetMapping("/{id}/detailed")
    public ResponseEntity<CompanyDetailsDTO> getDetailedCompanyById(@RequestHeader("Authorization") String token, @PathVariable("id") UUID id) {
        try {
            return ResponseEntity.ok(companyService.getDetailedCompanyById(id, token));
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (HttpClientErrorException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all companies with only their id and name.
     *
     * @return List of all companies with only their id and name.
     */
    @GetMapping("/minimized")
    public ResponseEntity<List<CompanyMinimizedDTO>> getAllCompaniesMinimized() {
        return ResponseEntity.ok(companyService.getAllCompaniesMinimized());
    }

    @GetMapping("/address-list/{id}")
    public ResponseEntity<List<UUID>> getCompanyAddressList(@PathVariable("id") UUID id) {
        try {
            return ResponseEntity.ok(companyService.getCompanyAddressList(id));
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create a new company.
     *
     * @param company Company to create.
     * @return Created company.
     */
    @PostMapping("/")
    public ResponseEntity<Company> createCompany(@RequestBody Company company) {
        try {
            return ResponseEntity.ok(companyService.createCompany(company));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update a company.
     *
     * @param company Company to update.
     * @return Updated company.
     */
    @PutMapping("/")
    public ResponseEntity<Company> updateCompany(@RequestBody Company company) {
        try {
            return ResponseEntity.ok(companyService.updateCompany(company));
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (HttpClientErrorException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete a company.
     *
     * @param id Company id.
     * @return True if the company was deleted, false otherwise.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteCompany(@PathVariable("id") UUID id) {
        try {
            companyService.deleteCompany(id);
            return ResponseEntity.ok(true);
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Change the company's logo.
     *
     * @param id Company id.
     * @param file New logo.
     * @return True if the logo was changed, false otherwise.
     */
    @PatchMapping("/logo/{id}")
    public ResponseEntity<Boolean> changeCompanyLogo(@PathVariable("id") UUID id, @RequestParam("file") MultipartFile file) {
        try {
            logger.info("Changing logo of company with id " + id);
            Company company = companyService.getCompanyById(id);

            String bucketName = "logo-" + id.toString();

            logger.info("Uploading logo to bucket " + bucketName);

            minioService.uploadFile(bucketName, "logo", file, true);

            logger.info("Setting logo url to " + "http://localhost:9000/" + bucketName + "/logo");

            // TODO: Change base url to a variable.
            company.setLogoUrl("http://localhost:9000/" + bucketName + "/logo");

            logger.info("Updating company");

            companyService.updateCompany(company);

            logger.info("Logo changed successfully");

            return ResponseEntity.ok(true);
        } catch (HttpClientErrorException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            return ResponseEntity.badRequest().build();
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (MinioException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add a document to the company.
     *
     * @param id Company id.
     * @param file Document to add.
     * @return True if the document was added, false otherwise.
     */
    @PatchMapping("/document/{id}")
    public ResponseEntity<Boolean> addCompanyDocument(@PathVariable("id") UUID id, @RequestParam("document") MultipartFile file) {
        try {
            logger.info("Adding document to company with id " + id);
            Company company = companyService.getCompanyById(id);

            String bucketName = "documents-" + id.toString();

            minioService.uploadFile(bucketName, file.getOriginalFilename(), file, false);

            List<String> documentsUrl = company.getDocumentsUrl();

            documentsUrl.add("http://localhost:8090/" + bucketName + "/" + file.getOriginalFilename());

            companyService.updateCompany(company);

            return ResponseEntity.ok(true);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | HttpClientErrorException e) {
            return ResponseEntity.badRequest().build();
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (MinioException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get the private URL of a document.
     *
     * @param id: The id of the company.
     * @param objectName: The name of the object.
     * @return The private URL of the document.
     */
    @GetMapping("/document/{id}/{objectName}")
    public ResponseEntity<String> getDocument(@PathVariable("id") UUID id, @PathVariable("objectName") String objectName){
        // TODO verify that document belongs to company
        // TODO verify that document exists
        // TODO verify that user is allowed to access document
        try {
            return ResponseEntity.ok(minioService.getPrivateDocumentUrl("documents-" + id.toString(), objectName));
        } catch (MinioException | IOException e) {
            return ResponseEntity.internalServerError().build();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/document/{id}/{objectName}")
    public ResponseEntity<Boolean> deleteDocument(@PathVariable("id") UUID id, @PathVariable("objectName") String objectName){
        // TODO verify that document belongs to company
        // TODO verify that document exists
        // TODO verify that user is allowed to access document
        try {
            minioService.deleteFileFromPrivateBucket("documents-" + id.toString(), objectName);

            Company company = companyService.getCompanyById(id);

            List<String> documentsUrl = company.getDocumentsUrl();

            documentsUrl.remove("http://localhost:8090/" + "documents-" + id.toString() + "/" + objectName);

            companyService.updateCompany(company);
            return ResponseEntity.ok(true);
        } catch (MinioException | IOException e) {
            return ResponseEntity.internalServerError().build();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
