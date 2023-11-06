package se.sundsvall.document.integration.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.document.integration.db.model.DocumentDataEntity;
import se.sundsvall.document.integration.db.model.DocumentEntity;

@CircuitBreaker(name = "documentDataRepository")
public interface DocumentDataRepository extends JpaRepository<DocumentDataEntity, String>, JpaSpecificationExecutor<DocumentEntity> {
}
