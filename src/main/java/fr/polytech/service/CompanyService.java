package fr.polytech.service;

import fr.polytech.model.AddressDTO;
import fr.polytech.model.Company;
import fr.polytech.model.CompanyDetailsDTO;
import fr.polytech.model.CompanyMinimizedDTO;
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

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CompanyRepository companyRepository;

    /**
     * Create a company.
     *
     * @param company The company to create.
     * @return The created company.
     * @throws HttpClientErrorException If the address microservice returns an error.
     */
    public Company createCompany(CompanyDetailsDTO company) throws HttpClientErrorException{

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
                "lb://address-api/api/v1/address/",
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

    /**
     * Get all companies.
     */
    public List<Company> getAllCompanies() {
        logger.info("Getting all companies");
        return companyRepository.findAll();
    }

    /**
     * Get a company by its id.
     *
     * @param id: the id of the company to return.
     * @return the company.
     * @throws NotFoundException if the company was not found.
     */
    public Company getCompanyById(UUID id) throws NotFoundException {
        Company company = companyRepository.findById(id).orElse(null);
        logger.info("Getting company with id " + id);

        if (company == null) {
            logger.error("Error while getting a company: company not found");
            // If the company is not found, throw an exception
            throw new NotFoundException("Company not found");
        }

        logger.info("Returning company with id " + id);
        return company;
    }

    public List<CompanyMinimizedDTO> getAllCompaniesMinimized() {
        logger.info("Getting all companies");
        return companyRepository.getAllCompaniesMinimized();
    }

    /**
     * Get the detailed version of a company by its id.
     *
     * @param id: the id of the company to return.
     * @return a detailed version of the company.
     * @throws NotFoundException if the company was not found.
     * @throws HttpClientErrorException if the address microservice returns an error.
     */
    public CompanyDetailsDTO getDetailedCompanyById(UUID id) throws NotFoundException, HttpClientErrorException {
        Company company = companyRepository.findById(id).orElse(null);

        if (company != null) {
            // Fetching address infos from address microservice
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<UUID> requestEntity = new HttpEntity<>(null, headers);

            // Sending the request to address microservice
            ResponseEntity<AddressDTO> responseEntity = restTemplate.exchange(
                    "lb://address-api/api/v1/address/" + company.getAddressId(),
                    HttpMethod.GET,
                    requestEntity,
                    AddressDTO.class
            );

            if(responseEntity.getStatusCode() != HttpStatus.OK){
                logger.error("Error while fetching address infos while getting a company");
                // If the status code is not 200, then throw the exception to the client
                throw new HttpClientErrorException(responseEntity.getStatusCode());
            }

            AddressDTO addressDTO = responseEntity.getBody();

            // Return the detailed company
            return new CompanyDetailsDTO(company.getId(), company.getName(), company.getLogoUrl(), company.getDescription(), company.getEmployeesNumberRange(), addressDTO.getStreet(), addressDTO.getNumber(), addressDTO.getCity(), addressDTO.getZipCode(), addressDTO.getCountry(), company.getSiretNumber(), company.getDocumentsUrl());
        }

        // If the company is not found, throw an exception
        throw new NotFoundException("Company not found");
    }

    /**
     * Update a company.
     *
     * @param company The company to update.
     * @return The updated company.
     * @throws NotFoundException If the company was not found.
     * @throws HttpClientErrorException If the address microservice returns an error.
     */
    public Company updateCompany(CompanyDetailsDTO company) throws NotFoundException, HttpClientErrorException{
        logger.info("Starting the update of a company");

        Company storedCompany = companyRepository.findById(company.getId()).orElse(null);

        if (storedCompany == null) {
            logger.error("Error while updating a company: company not found");
            // If the company is not found, throw an exception
            throw new NotFoundException("Company not found");
        }

        // TODO extract method
        storedCompany.setDescription(company.getDescription());
        storedCompany.setEmployeesNumberRange(company.getEmployeesNumberRange());
        storedCompany.setLogoUrl(company.getLogoUrl());
        storedCompany.setName(company.getName());

        // Create new address DTO
        AddressDTO addressDTO = new AddressDTO(company.getAddressStreet(), company.getAddressNumber(), company.getAddressCity(), company.getAddressZipCode(), company.getAddressCountry());

        // Creating address in address microservice
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AddressDTO> requestEntity = new HttpEntity<>(addressDTO, headers);

        // Sending the request to address microservice
        ResponseEntity<UUID> responseEntity = restTemplate.exchange(
                "lb://address-api/api/v1/address/",
                HttpMethod.POST,
                requestEntity,
                UUID.class
        );

        // Get the response, if the status code is 201, then the address was created successfully
        if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
            UUID createdAddressId = responseEntity.getBody();
            storedCompany.setAddressId(createdAddressId);
        } else {
            logger.error("Error while creating an address while creating a company");
            // If the status code is not 201, then throw the exception to the client
            throw new HttpClientErrorException(responseEntity.getStatusCode());
        }

        logger.info("Completed update of a company");
        return companyRepository.save(storedCompany);
    }

    /**
     * Update a company.
     *
     * @param company The company to update.
     * @return The updated company.
     * @throws NotFoundException If the company was not found.
     * @throws HttpClientErrorException If the address microservice returns an error.
     */
    public Company updateCompany(Company company) {
        logger.info("Starting the update of a company");

        Company storedCompany = companyRepository.findById(company.getId()).orElse(null);

        if (storedCompany == null) {
            logger.error("Error while updating a company: company not found");
            // If the company is not found, throw an exception
            throw new NotFoundException("Company not found");
        }

        storedCompany.setDescription(company.getDescription());
        storedCompany.setEmployeesNumberRange(company.getEmployeesNumberRange());
        storedCompany.setLogoUrl(company.getLogoUrl());
        storedCompany.setName(company.getName());
        storedCompany.setAddressId(company.getAddressId());
        storedCompany.setSiretNumber(company.getSiretNumber());
        storedCompany.setDocumentsUrl(company.getDocumentsUrl());

        logger.info("Completed update of a company");
        return companyRepository.save(storedCompany);
    }

    /**
     * Delete a company by its id.
     */
    public void deleteCompany(UUID id) throws NotFoundException {
        logger.info("Starting the deletion of a company");
        Company storedCompany = companyRepository.findById(id).orElse(null);

        if (storedCompany == null) {
            logger.error("Error while deleting a company: company not found");
            // If the company is not found, throw an exception
            throw new NotFoundException("Company not found");
        }

        logger.info("Completed deletion of a company");

        // Delete the company
        companyRepository.deleteById(id);
    }

}
