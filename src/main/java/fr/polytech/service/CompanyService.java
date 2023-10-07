package fr.polytech.service;

import fr.polytech.model.AddressDTO;
import fr.polytech.model.Company;
import fr.polytech.model.CompanyDetailsDTO;
import fr.polytech.repository.CompanyRepository;
import jakarta.ws.rs.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Service
public class CompanyService {

    // Initializing logger
    private final Logger logger = LoggerFactory.getLogger(CompanyService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private CompanyRepository companyRepository;

    /*
    * Create a company.
    * @param company The company to create.
    * @return The created company.
    * @throws HttpClientErrorException If the address microservice returns an error.
     */
    public Company createCompany(CompanyDetailsDTO company) {

        logger.info("Starting the creation of a company");

        // Create a new company
        Company companyReturn = new Company();
        companyReturn.setDescription(company.getDescription());
        companyReturn.setEmployeesNumberRange(company.getEmployeesNumberRange());
        companyReturn.setLogoUrl(company.getLogoUrl());
        companyReturn.setName(company.getName());

        // Create new address DTO
        AddressDTO addressDTO = new AddressDTO(company.getAddressStreet(), company.getAddressNumber(), company.getAddressCity(), company.getAddressZipCode(), company.getAddressCountry());

        // Creating address in address microservice
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AddressDTO> requestEntity = new HttpEntity<>(addressDTO, headers);

        // Sending the request to address microservice
        ResponseEntity<UUID> responseEntity = restTemplate.exchange(
                "lb://address-api/api/v1/address/", // Remplacez par l'URL correcte de votre adresse API.
                HttpMethod.POST,
                requestEntity,
                UUID.class
        );

        // Get the response, if the status code is 201, then the address was created successfully
        if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
            UUID createdAddressId = responseEntity.getBody();
            companyReturn.setAddressId(createdAddressId);
        } else {
            logger.error("Error while creating an address while creating a company");
            // If the status code is not 201, then throw the exception to the client
            throw new HttpClientErrorException(responseEntity.getStatusCode());
        }

        logger.info("Completed creation of a company");
        logger.debug("Created new company: " + companyReturn.toString());

        // Save the company in the database and return it
        return companyRepository.save(companyReturn);
    }

    /*
    * Get all companies.
     */
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    // TODO verify if this request works
    public CompanyDetailsDTO getCompanyById(UUID id) throws NotFoundException {
        Company company = companyRepository.findById(id).orElse(null);

        String adresseServiceUrl = "lb://address-api/api/v1/address/";
        AddressDTO addressDTO = null;
        if (company != null) {
            addressDTO = restTemplate.getForObject(adresseServiceUrl,  AddressDTO.class, company.getAddressId());

            return new CompanyDetailsDTO(company.getId(), company.getName(), company.getLogoUrl(), company.getDescription(), company.getEmployeesNumberRange(), addressDTO.getStreet(), addressDTO.getNumber(), addressDTO.getCity(), addressDTO.getZipCode(), addressDTO.getCountry());
        }

        throw new NotFoundException("Company not found");
    }

//    public Company updateCompany(CompanyDetails company) {
//        Company storedCompany = companyRepository.findById(company.getId()).orElse(null);
//
//        if (storedCompany == null) {
//            //TODO throw exception instead
//            return null;
//        }
//
//        // TODO extract method
//        storedCompany.setDescription(company.getDescription());
//        storedCompany.setEmployeesNumberRange(company.getEmployeesNumberRange());
//        storedCompany.setLogoUrl(company.getLogoUrl());
//        storedCompany.setName(company.getName());
//
//        // Créez un en-tête avec le type de contenu approprié
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//
//        // Configurez votre demande avec l'en-tête
////        HttpEntity<AddressDTO> requestEntity = new HttpEntity<>(company.getAddressDTO(), headers);
//
//        String adresseServiceUrl = "lb://address-api/api/v1/address/";
////        UUID addressId = restTemplate.postForObject(adresseServiceUrl, requestEntity, UUID.class);
////        ResponseEntity<UUID> addressId = restTemplate.exchange(adresseServiceUrl, HttpMethod.POST, company.getAddress(), UUID.class);
//
////        if (addressId != null) {
////            storedCompany.setAddressId(addressId);
////        }
////        else{
////            //TODO throw exception instead
////            return null;
////        }
//
//        return companyRepository.save(storedCompany);
//    }

    /*
    * Delete a company by its id.
     */
    public void deleteCompany(UUID id) {
        companyRepository.deleteById(id);
    }

}
