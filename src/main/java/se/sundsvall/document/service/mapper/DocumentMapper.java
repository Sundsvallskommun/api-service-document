package se.sundsvall.document.service.mapper;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

import se.sundsvall.document.api.model.Document;
import se.sundsvall.document.api.model.DocumentCreateRequest;
import se.sundsvall.document.api.model.DocumentMetadata;
import se.sundsvall.document.api.model.DocumentUpdateRequest;
import se.sundsvall.document.integration.db.DatabaseHelper;
import se.sundsvall.document.integration.db.model.DocumentDataEntity;
import se.sundsvall.document.integration.db.model.DocumentEntity;
import se.sundsvall.document.integration.db.model.DocumentMetadataEmbeddable;

public class DocumentMapper {

	private DocumentMapper() {}

	/**
	 * API to Database mappings.
	 */

	public static DocumentEntity toDocumentEntity(Document document) {
		return Optional.ofNullable(document)
			.map(doc -> DocumentEntity.create()
				.withCreated(doc.getCreated())
				.withCreatedBy(doc.getCreatedBy())
				.withId(doc.getId())
				.withMetadata(toDocumentMetadataEmbeddableList(doc.getMetadataList()))
				.withMunicipalityId(doc.getMunicipalityId())
				.withRegistrationNumber(doc.getRegistrationNumber())
				.withRevision(doc.getRevision()))
			.orElse(null);
	}

	public static DocumentEntity toDocumentEntity(DocumentCreateRequest documentCreateRequest) {
		return Optional.ofNullable(documentCreateRequest)
			.map(doc -> DocumentEntity.create()
				.withCreatedBy(doc.getCreatedBy())
				.withMetadata(toDocumentMetadataEmbeddableList(doc.getMetadataList()))
				.withMunicipalityId(doc.getMunicipalityId()))
			.orElse(null);
	}

	public static DocumentEntity toDocumentEntity(DocumentUpdateRequest documentUpdateRequest) {
		return Optional.ofNullable(documentUpdateRequest)
			.map(doc -> DocumentEntity.create()
				.withCreatedBy(doc.getCreatedBy())
				.withMetadata(Optional.ofNullable(doc.getMetadataList())
					.map(DocumentMapper::toDocumentMetadataEmbeddableList)
					.orElse(null)))
			.orElse(null);
	}

	public static DocumentDataEntity toDocumentDataEntity(MultipartFile multipartFile, DatabaseHelper databaseHelper) {
		return Optional.ofNullable(multipartFile)
			.map(file -> DocumentDataEntity.create()
				.withMimeType(file.getContentType())
				.withFile(databaseHelper.convertToBlob(file))
				.withFileName(file.getOriginalFilename()))
			.orElse(null);
	}

	public static DocumentDataEntity toDocumentDataEntity(DocumentDataEntity documentDataEntity) {
		return Optional.ofNullable(documentDataEntity)
			.map(doc -> DocumentDataEntity.create()
				.withMimeType(doc.getMimeType())
				.withFile(doc.getFile())
				.withFileName(doc.getFileName()))
			.orElse(null);
	}

	private static List<DocumentMetadataEmbeddable> toDocumentMetadataEmbeddableList(List<DocumentMetadata> documentMetadataList) {
		return Optional.ofNullable(documentMetadataList).orElse(emptyList()).stream()
			.map(documentMetadata -> DocumentMetadataEmbeddable.create()
				.withKey(documentMetadata.getKey())
				.withValue(documentMetadata.getValue()))
			.toList();
	}

	/**
	 * Database to API mappings.
	 */

	public static List<Document> toDocumentList(List<DocumentEntity> documentEntityList) {
		return Optional.ofNullable(documentEntityList).orElse(emptyList()).stream()
			.map(DocumentMapper::toDocument)
			.toList();
	}

	public static Document toDocument(DocumentEntity documentEntity) {
		return Optional.ofNullable(documentEntity).map(docEntity -> Document.create()
			.withCreated(docEntity.getCreated())
			.withCreatedBy(docEntity.getCreatedBy())
			.withId(docEntity.getId())
			.withMetadataList(toDocumentMetadataList(docEntity.getMetadata()))
			.withMunicipalityId(docEntity.getMunicipalityId())
			.withRegistrationNumber(docEntity.getRegistrationNumber())
			.withRevision(docEntity.getRevision())).orElse(null);
	}

	private static List<DocumentMetadata> toDocumentMetadataList(List<DocumentMetadataEmbeddable> documentMetadataEmbeddableList) {
		return Optional.ofNullable(documentMetadataEmbeddableList).orElse(emptyList()).stream()
			.map(docMetadataEmbeddable -> DocumentMetadata.create()
				.withKey(docMetadataEmbeddable.getKey())
				.withValue(docMetadataEmbeddable.getValue()))
			.toList();
	}
}
