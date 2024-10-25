package se.sundsvall.document.integration.db;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.document.integration.db.model.DocumentTypeEntity;

@CircuitBreaker(name = "documentTypeRepository")
public interface DocumentTypeRepository extends JpaRepository<DocumentTypeEntity, String> {}
