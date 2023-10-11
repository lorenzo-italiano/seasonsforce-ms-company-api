package fr.polytech.restcontroller;

import fr.polytech.model.Company;
import fr.polytech.model.CompanyDetailsDTO;
import fr.polytech.service.CompanyService;
import fr.polytech.service.HashService;
import fr.polytech.service.MinioService;
import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/company")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private MinioService minioService;

    @Autowired
    private HashService hashService;

    @GetMapping("/")
    public ResponseEntity<List<Company>> getAllCompanies() {
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompanyById(@PathVariable("id") UUID id) {
        try {
            return ResponseEntity.ok(companyService.getCompanyById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/detailed")
    public ResponseEntity<CompanyDetailsDTO> getDetailedCompanyById(@PathVariable("id") UUID id) {
        try {
            return ResponseEntity.ok(companyService.getDetailedCompanyById(id));
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/")
    public ResponseEntity<Company> createCompany(@RequestBody CompanyDetailsDTO company) {
        try {
            return ResponseEntity.ok(companyService.createCompany(company));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/")
    public ResponseEntity<Company> updateCompany(@RequestBody CompanyDetailsDTO company) {
        try {
            return ResponseEntity.ok(companyService.updateCompany(company));
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteCompany(@PathVariable("id") UUID id) {
        try {
            companyService.deleteCompany(id);
            return ResponseEntity.ok(true);
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/logo/{id}")
    public ResponseEntity<String> changeCompanyLogo(@PathVariable("id") UUID id, @RequestParam("file") MultipartFile file) {
        try {
            CompanyDetailsDTO company = companyService.getDetailedCompanyById(id);

            String hashedBucketName = hashService.hash("logo-" + id.toString());

            System.out.println(hashedBucketName);

            minioService.uploadFile(hashedBucketName, "logo", file);

            // TODO: Change base url to a variable.
            company.setLogoUrl("http://localhost:9000/" + hashedBucketName + "/logo");

            companyService.updateCompany(company);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/document/{id}")
    public ResponseEntity<String> addCompanyDocument(@PathVariable("id") UUID id, @RequestParam("file") MultipartFile file) {
        try {
            CompanyDetailsDTO company = companyService.getDetailedCompanyById(id);

            String hashedBucketName = hashService.hash("document-" + id.toString());

            System.out.println(hashedBucketName);

            minioService.uploadFile(hashedBucketName, file.getOriginalFilename(), file);

            List<String> documentsUrl = company.getDocumentsUrl();

            documentsUrl.add("http://localhost:9000/" + hashedBucketName + "/" + file.getOriginalFilename());

            companyService.updateCompany(company);

            return ResponseEntity.ok().build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
