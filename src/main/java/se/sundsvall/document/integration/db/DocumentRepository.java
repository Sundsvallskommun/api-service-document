package se.sundsvall.document.integration.db;

import static se.sundsvall.document.integration.db.specification.SearchSpecification.withSearchPhrase;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.document.integration.db.model.DocumentEntity;

@CircuitBreaker(name = "documentRepository")
public interface DocumentRepository extends JpaRepository<DocumentEntity, String>, JpaSpecificationExecutor<DocumentEntity> {

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
	 * @param  pageable           the pageable object.
	 * @return                    a Page of DocumentEntity objects.
	 */
	Page<DocumentEntity> findByRegistrationNumber(String registrationNumber, Pageable pageable);

	/**
	 * Find document by registrationNumber and revision.
	 *
	 * @param  registrationNumber of the DocumentEntity.
	 * @param  revision           Document revision number.
	 * @return                    an Optional of DocumentEntity object.
	 */
	Optional<DocumentEntity> findByRegistrationNumberAndRevision(String registrationNumber, int revision);

	/**
	 * Performs a search in the DocumentEntitties.
	 *
	 * @param  query    the string to search for.
	 * @param  pageable the pageable object.
	 * @return          a Page of DocumentEntity objects that matches the search string.
	 */
	default Page<DocumentEntity> search(String query, Pageable pageable) {
		return this.findAll(withSearchPhrase(query), pageable);
	}
}
