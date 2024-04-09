package se.sundsvall.document.integration.db;

import static se.sundsvall.document.integration.db.specification.SearchSpecification.withSearchQuery;

import java.util.List;
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
	 * @param  confidentialValues values of confidentiality for the documents that should be included in the result where
	 *                            true equals confidential document, false equals public document.
	 * @return                    an Optional of DocumentEntity object.
	 */
	Optional<DocumentEntity> findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(String registrationNumber, List<Boolean> confidentialValues);

	/**
	 * Find all revisions of a document by registrationNumber.
	 *
	 * @param  registrationNumber of the DocumentEntity.
	 * @param  confidentialValues values of confidentiality for the documents that should be included in the result where
	 *                            true equals confidential document, false equals public document.
	 * @param  pageable           the pageable object.
	 * @return                    a Page of DocumentEntity objects.
	 */
	Page<DocumentEntity> findByRegistrationNumberAndConfidentialityConfidentialIn(String registrationNumber, List<Boolean> confidentialValues, Pageable pageable);

	/**
	 * Find all revisions of a document by registrationNumber.
	 *
	 * @param  registrationNumber of the DocumentEntity.
	 * @param  confidentialValues values of confidentiality for the documents that should be included in the result where
	 *                            true equals confidential document, false equals public document.
	 * @return                    a List of DocumentEntity objects.
	 */
	List<DocumentEntity> findByRegistrationNumberAndConfidentialityConfidentialIn(String registrationNumber, List<Boolean> confidentialValues);

	/**
	 * Find document by registrationNumber and revision.
	 *
	 * @param  registrationNumber of the DocumentEntity.
	 * @param  revision           Document revision number.
	 * @param  confidentialValues values of confidentiality for the documents that should be included in the result where
	 *                            true equals confidential document, false equals public document.
	 * @return                    an Optional of DocumentEntity object.
	 */
	Optional<DocumentEntity> findByRegistrationNumberAndRevisionAndConfidentialityConfidentialIn(String registrationNumber, int revision, List<Boolean> confidentialValues);

	/**
	 * Performs a search in DocumentEntities.
	 *
	 * @param  query               the string to search for.
	 * @param  includeConfidential option if confidential documents should be included or not.
	 * @param  pageable            the pageable object.
	 * @return                     a Page of DocumentEntity objects that matches the search string.
	 */
	default Page<DocumentEntity> search(String query, boolean includeConfidential, Pageable pageable) {
		return this.findAll(withSearchQuery(query, includeConfidential), pageable);
	}
}
