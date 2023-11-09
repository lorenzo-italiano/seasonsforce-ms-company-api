package fr.polytech.model;

import java.util.List;
import java.util.UUID;

// This DTO is used to get data from front and save it as multiple objects
public class CompanyDetailsDTO {

    private UUID id;
    private String name;
    private String logoUrl;
    private String description;
    private String employeesNumberRange;
    private List<AddressDTO> addressList;
    private String siretNumber;

    private List<String> documentsUrl;

    public CompanyDetailsDTO(UUID id, String name, String logoUrl, String description, List<AddressDTO> addressList, String employeesNumberRange, String siretNumber, List<String> documentsUrl) {
        this.id = id;
        this.name = name;
        this.logoUrl = logoUrl;
        this.description = description;
        this.employeesNumberRange = employeesNumberRange;
        this.addressList = addressList;
        this.siretNumber = siretNumber;
        this.documentsUrl = documentsUrl;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmployeesNumberRange() {
        return employeesNumberRange;
    }

    public void setEmployeesNumberRange(String employeesNumberRange) {
        this.employeesNumberRange = employeesNumberRange;
    }

    public List<AddressDTO> getAddressList() {
        return addressList;
    }

    public void setAddressList(List<AddressDTO> addressList) {
        this.addressList = addressList;
    }

    public String getSiretNumber() {
        return siretNumber;
    }

    public void setSiretNumber(String siretNumber) {
        this.siretNumber = siretNumber;
    }

    public List<String> getDocumentsUrl() {
        return documentsUrl;
    }

    public void setDocumentsUrl(List<String> documentsUrl) {
        this.documentsUrl = documentsUrl;
    }
}
