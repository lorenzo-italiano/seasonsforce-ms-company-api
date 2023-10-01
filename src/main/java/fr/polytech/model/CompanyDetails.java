package fr.polytech.model;

import java.util.UUID;

public class CompanyDetails {

    private UUID id;

    private String name;

    private String logoUrl;

    private String description;

    private String employeesNumberRange;

    private AddressDTO address;

    public CompanyDetails(UUID id, String name, String logoUrl, String description, String employeesNumberRange, AddressDTO address) {
        this.id = id;
        this.name = name;
        this.logoUrl = logoUrl;
        this.description = description;
        this.employeesNumberRange = employeesNumberRange;
        this.address = address;
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

    public AddressDTO getAddress() {
        return address;
    }

    public void setAddress(AddressDTO address) {
        this.address = address;
    }
}
