package se.sundsvall.document.integration.db.specification;

import static jakarta.persistence.criteria.JoinType.LEFT;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.data.jpa.domain.Specification.where;
import static se.sundsvall.document.integration.db.model.ConfidentialityEmbeddable_.CONFIDENTIAL;
import static se.sundsvall.document.integration.db.model.DocumentDataEntity_.FILE_NAME;
import static se.sundsvall.document.integration.db.model.DocumentDataEntity_.MIME_TYPE;
import static se.sundsvall.document.integration.db.model.DocumentEntity_.CONFIDENTIALITY;
import static se.sundsvall.document.integration.db.model.DocumentEntity_.CREATED_BY;
import static se.sundsvall.document.integration.db.model.DocumentEntity_.DESCRIPTION;
import static se.sundsvall.document.integration.db.model.DocumentEntity_.DOCUMENT_DATA;
import static se.sundsvall.document.integration.db.model.DocumentEntity_.METADATA;
import static se.sundsvall.document.integration.db.model.DocumentEntity_.MUNICIPALITY_ID;
import static se.sundsvall.document.integration.db.model.DocumentEntity_.REGISTRATION_NUMBER;
import static se.sundsvall.document.integration.db.model.DocumentMetadataEmbeddable_.KEY;
import static se.sundsvall.document.integration.db.model.DocumentMetadataEmbeddable_.VALUE;

import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;

import se.sundsvall.document.integration.db.model.DocumentEntity;

public interface SearchSpecification {

	static Specification<DocumentEntity> withSearchQuery(String query, boolean includeConfidential) {
		final var queryString = toQueryString(query);
		return where(matchesCreatedBy(queryString))
			.or(matchesDescription(queryString))
			.or(matchesMunicipalityId(queryString))
			.or(matchesRegistrationNumber(queryString))
			.or(matchesFileName(queryString))
			.or(matchesMimeType(queryString))
			.or(matchesMetadataKey(queryString))
			.or(matchesMetadataValue(queryString))
			.and(includeConfidentialDocuments(includeConfidential))
			.and(distinct());
	}

	private static Specification<DocumentEntity> includeConfidentialDocuments(boolean includeConfidential) {
		if (includeConfidential) {
			return null; // Do not add any filter to return all documents regardless of whether they are confidential or not
		}
		return (entity, cq, cb) -> cb.equal(entity.get(CONFIDENTIALITY).get(CONFIDENTIAL), false); // Return non-confidential documents only
	}

	private static Specification<DocumentEntity> matchesCreatedBy(String query) {
		return (entity, cq, cb) -> cb.like(cb.lower(entity.get(CREATED_BY)), query);
	}

	private static Specification<DocumentEntity> matchesDescription(String query) {
		return (entity, cq, cb) -> cb.like(cb.lower(entity.get(DESCRIPTION)), query);
	}

	private static Specification<DocumentEntity> matchesMunicipalityId(String query) {
		return (entity, cq, cb) -> cb.like(cb.lower(entity.get(MUNICIPALITY_ID)), query);
	}

	private static Specification<DocumentEntity> matchesRegistrationNumber(String query) {
		return (entity, cq, cb) -> cb.like(cb.lower(entity.get(REGISTRATION_NUMBER)), query);
	}

	private static Specification<DocumentEntity> matchesFileName(String query) {
		return (entity, cq, cb) -> cb.like(cb.lower(entity.join(DOCUMENT_DATA, LEFT).get(FILE_NAME)), query);
	}

	private static Specification<DocumentEntity> matchesMimeType(String query) {
		return (entity, cq, cb) -> cb.like(cb.lower(entity.join(DOCUMENT_DATA, LEFT).get(MIME_TYPE)), query);
	}

	private static Specification<DocumentEntity> matchesMetadataKey(String query) {
		return (entity, cq, cb) -> cb.like(cb.lower(entity.join(METADATA, LEFT).get(KEY)), query);
	}

	private static Specification<DocumentEntity> matchesMetadataValue(String query) {
		return (entity, cq, cb) -> cb.like(cb.lower(entity.join(METADATA, LEFT).get(VALUE)), query);
	}

	private static Specification<DocumentEntity> distinct() {
		return (entity, cq, cb) -> {
			cq.distinct(true);
			return null;
		};
	}

	private static String toQueryString(String query) {
		return Optional.ofNullable(query)
			.map(String::trim)
			.map(String::toLowerCase)
			.map(str -> str.replace('*', '%'))
			.orElse(EMPTY);
	}
}
