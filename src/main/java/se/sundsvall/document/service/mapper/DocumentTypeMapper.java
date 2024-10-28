package se.sundsvall.document.service.mapper;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.anyNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import se.sundsvall.document.api.model.DocumentType;
import se.sundsvall.document.api.model.DocumentTypeCreateRequest;
import se.sundsvall.document.api.model.DocumentTypeUpdateRequest;
import se.sundsvall.document.integration.db.model.DocumentTypeEntity;

public class DocumentTypeMapper {

	private DocumentTypeMapper() {}

	/**
	 * API to Database mappings.
	 */

	public static DocumentTypeEntity toDocumentTypeEntity(String municipalityId, DocumentTypeCreateRequest documentTypeCreateRequest) {
		return ofNullable(documentTypeCreateRequest)
			.map(doc -> DocumentTypeEntity.create()
				.withCreatedBy(documentTypeCreateRequest.getCreatedBy())
				.withDisplayName(documentTypeCreateRequest.getDisplayName())
				.withMunicipalityId(municipalityId)
				.withType(ofNullable(documentTypeCreateRequest.getType()).map(String::toUpperCase).orElse(null)))
			.orElse(null);
	}

	public static DocumentTypeEntity updateDocumentTypeEntity(DocumentTypeEntity entity, DocumentTypeUpdateRequest documentTypeUpdateRequest) {
		if (anyNull(documentTypeUpdateRequest, entity)) {
			return entity;
		}

		ofNullable(documentTypeUpdateRequest.getDisplayName()).ifPresent(entity::setDisplayName);
		ofNullable(documentTypeUpdateRequest.getType()).map(String::toUpperCase).ifPresent(entity::setType);

		return entity.withLastUpdatedBy(documentTypeUpdateRequest.getUpdatedBy());
	}

	/**
	 * Database to API mappings.
	 */

	public static List<DocumentType> toDocumentTypes(List<DocumentTypeEntity> documentTypeEntities) {
		return Optional.ofNullable(documentTypeEntities).orElse(emptyList()).stream()
			.filter(Objects::nonNull)
			.sorted((a1, a2) -> a1.getDisplayName().compareTo(a2.getDisplayName()))
			.map(DocumentTypeMapper::toDocumentType)
			.toList();
	}

	public static DocumentType toDocumentType(DocumentTypeEntity documentTypeEntity) {
		return Optional.ofNullable(documentTypeEntity)
			.map(docEntity -> DocumentType.create()
				.withDisplayName(documentTypeEntity.getDisplayName())
				.withType(documentTypeEntity.getType()))
			.orElse(null);
	}
}
