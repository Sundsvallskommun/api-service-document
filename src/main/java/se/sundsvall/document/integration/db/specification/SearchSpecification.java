package se.sundsvall.document.integration.db.specification;

import static org.apache.commons.lang3.StringUtils.EMPTY;
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

		final var q = Optional.ofNullable(query)
			.map(String::trim)
			.map(str -> str.toLowerCase().replace('*', '%'))
			.orElse(EMPTY);

		return (documentEntity, cq, cb) -> cb.or(

			// Search in document.
			cb.like(cb.lower(documentEntity.get(CREATED_BY)), q),
			cb.like(cb.lower(documentEntity.get(MUNICIPALITY_ID)), q),
			cb.like(cb.lower(documentEntity.get(REGISTRATION_NUMBER)), q),

			// Search in document.documentData.
			cb.like(cb.lower(documentEntity.join(DOCUMENT_DATA).get(FILE_NAME)), q),
			cb.like(cb.lower(documentEntity.join(DOCUMENT_DATA).get(MIME_TYPE)), q),

			// Search in document.metadata.
			cb.like(cb.lower(documentEntity.join(METADATA).get(KEY)), q),
			cb.like(cb.lower(documentEntity.join(METADATA).get(VALUE)), q));
	}
}
