package fr.polytech.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import fr.polytech.model.*;
import fr.polytech.repository.CompanyRepository;
import jakarta.ws.rs.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CompanyService {

    // Initializing logger
    private final Logger logger = LoggerFactory.getLogger(CompanyService.class);
    private final String USER_API_URI = Optional.ofNullable(System.getenv("USER_API_URI")).orElse("lb://user-api/api/v1/user");
    private final String ADDRESS_API_URI = Optional.ofNullable(System.getenv("ADDRESS_API_URI")).orElse("lb://address-api/api/v1/address");

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
    public Company createCompany(Company company) throws HttpClientErrorException{
        logger.info("Starting the creation of a company");

        // Save the company in the database and return it
        return companyRepository.save(company);
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
    public CompanyDetailsDTO getDetailedCompanyById(UUID id, String token) throws NotFoundException, HttpClientErrorException {
        Company company = companyRepository.findById(id).orElse(null);

        if (company != null) {
            List<AddressDTO> addressDTOList = new ArrayList<>();

            if (!company.getAddressIdList().isEmpty()){
                for (UUID addressId: company.getAddressIdList()) {

                    // Fetching address infos from address microservice
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.setBearerAuth(token.split(" ")[1]);
                    HttpEntity<UUID> requestEntity = new HttpEntity<>(null, headers);

                    logger.info("trying to fetch address with id " + addressId);

                    // Sending the request to address microservice
                    ResponseEntity<AddressDTO> responseEntity = restTemplate.exchange(
                            ADDRESS_API_URI + "/" + addressId,
                            HttpMethod.GET,
                            requestEntity,
                            AddressDTO.class
                    );

                    if(responseEntity.getStatusCode() != HttpStatus.OK){
                        logger.info(responseEntity.getStatusCode().toString());
                        logger.error("Error while fetching address infos while getting a company");
                        // If the status code is not 200, then throw the exception to the client
                        throw new HttpClientErrorException(responseEntity.getStatusCode());
                    }

                    AddressDTO addressDTO = responseEntity.getBody();

                    addressDTOList.add(addressDTO);
                }
            }


            // Return the detailed company
            return new CompanyDetailsDTO(company.getId(), company.getName(), company.getLogoUrl(), company.getDescription(),  addressDTOList, company.getEmployeesNumberRange(), company.getSiretNumber(), company.getDocumentsUrl());
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
        storedCompany.setAddressIdList(company.getAddressIdList());
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

    public List<AddressDTO> getCompanyAddressList(UUID id, String token) {
        Company company = getCompanyById(id);

        List<UUID> addressList = company.getAddressIdList();

        List<AddressDTO> addressDTOList = new ArrayList<>();

        for (UUID addressId: addressList) {
            // Fetching address infos from address microservice
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            HttpEntity<UUID> requestEntity = new HttpEntity<>(null, headers);

            logger.info("trying to fetch address with id " + addressId);

            // Sending the request to address microservice
            ResponseEntity<AddressDTO> responseEntity = restTemplate.exchange(
                    ADDRESS_API_URI + "/" + addressId,
                    HttpMethod.GET,
                    requestEntity,
                    AddressDTO.class
            );

            if(responseEntity.getStatusCode() != HttpStatus.OK){
                logger.info(responseEntity.getStatusCode().toString());
                logger.error("Error while fetching address infos while getting a company");
                // If the status code is not 200, then throw the exception to the client
                throw new HttpClientErrorException(responseEntity.getStatusCode());
            }

            AddressDTO addressDTO = responseEntity.getBody();

            addressDTOList.add(addressDTO);
        }

        return addressDTOList;
    }

    /**
     * Check if a user is a member of the company.
     *
     * @param companyId: the id of the company.
     * @param bearerToken: the token of the user.
     * @return true if the user is a member of the company, false otherwise.
     * @throws HttpClientErrorException if the user microservice returns an error.
     */
    public boolean isUserMemberOfCompany(UUID companyId, String bearerToken) throws HttpClientErrorException {
        logger.info("Checking if user is a member of the company");

        String token = bearerToken.split(" ")[1];
        DecodedJWT jwt = JWT.decode(token);
        Claim subClaim = jwt.getClaim("sub");
        UUID userId = UUID.fromString(subClaim.asString());

        // Fetching user infos from user microservice
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<RecruiterDTO> requestEntity = new HttpEntity<>(null, headers);

        logger.info("Fetching user with id " + userId);

        // Sending the request to user microservice
        ResponseEntity<RecruiterDTO> responseEntity = restTemplate.exchange(
                USER_API_URI + "/" + userId,
                HttpMethod.GET,
                requestEntity,
                RecruiterDTO.class
        );
        RecruiterDTO recruiterDTO = responseEntity.getBody();
        if (recruiterDTO == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "User not found");
        } else if (recruiterDTO.getRole() == null || !recruiterDTO.getRole().equals("recruiter") || recruiterDTO.getCompanyId() == null) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "User is not a recruiter");
        }

        return recruiterDTO.getCompanyId().equals(companyId);
    }
}
