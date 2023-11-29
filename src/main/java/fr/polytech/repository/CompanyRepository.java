package fr.polytech.repository;

import fr.polytech.model.Company;
import fr.polytech.model.CompanyMinimizedDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    @Query("SELECT new fr.polytech.model.CompanyMinimizedDTO(c.id, c.name) FROM Company c")
    List<CompanyMinimizedDTO> getAllCompaniesMinimized();
}
