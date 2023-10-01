package fr.polytech.service;

import fr.polytech.model.AddressDTO;
import fr.polytech.model.Company;
import fr.polytech.model.CompanyDetails;
import fr.polytech.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    public Company createCompany(CompanyDetails company) {

        // Create a new company
        Company companyReturn = new Company();
        companyReturn.setDescription(company.getDescription());
        companyReturn.setEmployeesNumberRange(company.getEmployeesNumberRange());
        companyReturn.setLogoUrl(company.getLogoUrl());
        companyReturn.setName(company.getName());

        // Create a new address for the company and get its id.
        RestTemplate restTemplate = new RestTemplate();
        String adresseServiceUrl = "lb://address-api/api/v1/address/";
        UUID addressId = restTemplate.postForObject(adresseServiceUrl, company.getAddress(), UUID.class);

        // Set the address id to the company.
        if (addressId != null) {
            companyReturn.setAddressId(addressId);
        }

        return companyRepository.save(companyReturn);
    }

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public CompanyDetails getCompanyById(UUID id) {
        Company company = companyRepository.findById(id).orElse(null);

        RestTemplate restTemplate = new RestTemplate();
        String adresseServiceUrl = "lb://address-api/api/v1/address/";
        AddressDTO addressDTO = null;
        if (company != null) {
            addressDTO = restTemplate.getForObject(adresseServiceUrl,  AddressDTO.class, company.getAddressId());
            return new CompanyDetails(company.getId(), company.getName(), company.getLogoUrl(), company.getDescription(), company.getEmployeesNumberRange(), addressDTO);
        }

        return null;
    }

    public Company updateCompany(CompanyDetails company) {
        Company storedCompany = companyRepository.findById(company.getId()).orElse(null);

        if (storedCompany == null) {
            //TODO throw exception instead
            return null;
        }

        // TODO extract method
        storedCompany.setDescription(company.getDescription());
        storedCompany.setEmployeesNumberRange(company.getEmployeesNumberRange());
        storedCompany.setLogoUrl(company.getLogoUrl());
        storedCompany.setName(company.getName());

        RestTemplate restTemplate = new RestTemplate();
        String adresseServiceUrl = "lb://address-api/api/v1/address/";
        UUID addressId = restTemplate.postForObject(adresseServiceUrl, company.getAddress(), UUID.class);

        if (addressId != null) {
            storedCompany.setAddressId(addressId);
        }
        else{
            //TODO throw exception instead
            return null;
        }

        return companyRepository.save(storedCompany);
    }

    /*
    * Delete a company by its id.
     */
    public void deleteCompany(UUID id) {
        companyRepository.deleteById(id);
    }

}
