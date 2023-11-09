package fr.polytech.model;

import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "company", schema = "public")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private String logoUrl;

    private String description;

    private String employeesNumberRange;

    @ElementCollection(targetClass = UUID.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "addresses", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "address", nullable = false)
    private List<UUID> addressIdList;

    private String siretNumber;

    @ElementCollection(targetClass = String.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "documents", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "document", nullable = false)
    private List<String> documentsUrl;

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

    public List<UUID> getAddressIdList() {
        return addressIdList;
    }

    public void setAddressIdList(List<UUID> addressIdList) {
        this.addressIdList = addressIdList;
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
