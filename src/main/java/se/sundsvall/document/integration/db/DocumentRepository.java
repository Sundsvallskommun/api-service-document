package se.sundsvall.document.integration.db;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.document.integration.db.model.DocumentEntity;

@CircuitBreaker(name = "documentRepository")
public interface DocumentRepository extends JpaRepository<DocumentEntity, String> {

	/**
	 * Find latest document by registrationNumber.
	 *
	 * @param  registrationNumber of the DocumentEntity.
	 * @return                    an Optional of DocumentEntity object.
	 */
	Optional<DocumentEntity> findTopByRegistrationNumberOrderByRevisionDesc(String registrationNumber);

	/**
	 * Find all revisions of a document by registrationNumber.
	 *
	 * @param  registrationNumber of the DocumentEntity.
	 * @return                    a List of DocumentEntity objects.
	 */
	List<DocumentEntity> findByRegistrationNumberOrderByRevisionAsc(String registrationNumber);

	/**
	 * Find document by registrationNumber and revision.
	 *
	 * @param  registrationNumber of the DocumentEntity.
	 * @param  revision           Document revision number.
	 * @return                    an Optional of DocumentEntity object.
	 */
	Optional<DocumentEntity> findByRegistrationNumberAndRevision(String registrationNumber, int revision);
}
