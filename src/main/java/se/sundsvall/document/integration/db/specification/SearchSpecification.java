package se.sundsvall.document.integration.db.specification;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.data.jpa.domain.Specification.where;
import static se.sundsvall.document.integration.db.model.DocumentDataEntity_.FILE_NAME;
import static se.sundsvall.document.integration.db.model.DocumentDataEntity_.MIME_TYPE;
import static se.sundsvall.document.integration.db.model.DocumentEntity_.CREATED_BY;
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

	static Specification<DocumentEntity> withSearchQuery(String query) {
		return where(matchesCreatedBy(query))
			.or(matchesMunicipalityId(query))
			.or(matchesRegistrationNumber(query))
			.or(matchesFileName(query))
			.or(matchesMimeType(query))
			.or(matchesMetadataKey(query))
			.or(matchesMetadataValue(query))
			.and(distinct());
	}

	static Specification<DocumentEntity> matchesCreatedBy(String query) {
		return (documentEntity, cq, cb) -> cb.like(cb.lower(documentEntity.get(CREATED_BY)), withQueryString(query));
	}

	static Specification<DocumentEntity> matchesMunicipalityId(String query) {
		return (documentEntity, cq, cb) -> cb.like(cb.lower(documentEntity.get(MUNICIPALITY_ID)), withQueryString(query));
	}

	static Specification<DocumentEntity> matchesRegistrationNumber(String query) {
		return (documentEntity, cq, cb) -> cb.like(cb.lower(documentEntity.get(REGISTRATION_NUMBER)), withQueryString(query));
	}

	static Specification<DocumentEntity> matchesFileName(String query) {
		return (documentEntity, cq, cb) -> cb.like(cb.lower(documentEntity.join(DOCUMENT_DATA).get(FILE_NAME)), withQueryString(query));
	}

	static Specification<DocumentEntity> matchesMimeType(String query) {
		return (documentEntity, cq, cb) -> cb.like(cb.lower(documentEntity.join(DOCUMENT_DATA).get(MIME_TYPE)), withQueryString(query));
	}

	static Specification<DocumentEntity> matchesMetadataKey(String query) {
		return (documentEntity, cq, cb) -> cb.like(cb.lower(documentEntity.join(METADATA).get(KEY)), withQueryString(query));
	}

	static Specification<DocumentEntity> matchesMetadataValue(String query) {
		return (documentEntity, cq, cb) -> cb.like(cb.lower(documentEntity.join(METADATA).get(VALUE)), withQueryString(query));
	}

	static Specification<DocumentEntity> distinct() {
		return (documentEntity, cq, cb) -> {
			cq.distinct(true);
			return null;
		};
	}

	static String withQueryString(String query) {
		return Optional.ofNullable(query)
			.map(String::trim)
			.map(String::toLowerCase)
			.map(str -> str.replace('*', '%'))
			.orElse(EMPTY);
	}
}
