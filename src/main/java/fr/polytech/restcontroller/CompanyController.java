package fr.polytech.restcontroller;

import fr.polytech.model.Company;
import fr.polytech.model.CompanyDetailsDTO;
import fr.polytech.service.CompanyService;
import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/company")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

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
    public ResponseEntity<Boolean> deleteCompany(UUID id) {
        try {
            companyService.deleteCompany(id);
            return ResponseEntity.ok(true);
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
