package se.sundsvall.document.integration.db;

import static se.sundsvall.document.integration.db.specification.SearchSpecification.withSearchQuery;

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
	 * @param registrationNumber  of the DocumentEntity.
	 * @param includeConfidential option if confidential documents should be included or not.
	 * @return an Optional of DocumentEntity object.
	 */
	Optional<DocumentEntity> findTopByRegistrationNumberAndConfidentialOrderByRevisionDesc(String registrationNumber, boolean includeConfidential);

	/**
	 * Find all revisions of a document by registrationNumber.
	 *
	 * @param registrationNumber  of the DocumentEntity.
	 * @param includeConfidential option if confidential documents should be included or not.
	 * @param pageable            the pageable object.
	 * @return a Page of DocumentEntity objects.
	 */
	Page<DocumentEntity> findByRegistrationNumberAndConfidential(String registrationNumber, boolean includeConfidential, Pageable pageable);

	/**
	 * Find document by registrationNumber and revision.
	 *
	 * @param registrationNumber  of the DocumentEntity.
	 * @param revision            Document revision number.
	 * @param includeConfidential option if confidential documents should be included or not.
	 * @return an Optional of DocumentEntity object.
	 */
	Optional<DocumentEntity> findByRegistrationNumberAndRevisionAndConfidential(String registrationNumber, int revision, boolean includeConfidential);

	/**
	 * Performs a search in DocumentEntities.
	 *
	 * @param query               the string to search for.
	 * @param includeConfidential option if confidential documents should be included or not.
	 * @param pageable            the pageable object.
	 * @return a Page of DocumentEntity objects that matches the search string.
	 */
	default Page<DocumentEntity> search(String query, boolean includeConfidential, Pageable pageable) {
		return this.findAll(withSearchQuery(query, includeConfidential), pageable);
	}
}
