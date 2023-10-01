package fr.polytech.restcontroller;

import fr.polytech.model.Company;
import fr.polytech.model.CompanyDetails;
import fr.polytech.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/company")
// TODO handle errors and exceptions.
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @GetMapping("/")
    public List<Company> getAllCompanies() {
        return companyService.getAllCompanies();
    }

    @GetMapping("/{id}")
    public CompanyDetails getCompanyById(UUID id) {
        return companyService.getCompanyById(id);
    }

    @PostMapping("/")
    public Company createCompany(CompanyDetails company) {
        return companyService.createCompany(company);
    }

    @PutMapping("/")
    public Company updateCompany(CompanyDetails company) {
        return companyService.updateCompany(company);
    }

    @DeleteMapping("/{id}")
    public void deleteCompany(UUID id) {
        companyService.deleteCompany(id);
    }

}
