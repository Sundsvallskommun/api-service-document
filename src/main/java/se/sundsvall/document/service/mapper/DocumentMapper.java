package se.sundsvall.document.service.mapper;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import se.sundsvall.dept44.models.api.paging.PagingMetaData;
import se.sundsvall.document.api.model.Document;
import se.sundsvall.document.api.model.DocumentCreateRequest;
import se.sundsvall.document.api.model.DocumentData;
import se.sundsvall.document.api.model.DocumentMetadata;
import se.sundsvall.document.api.model.DocumentUpdateRequest;
import se.sundsvall.document.api.model.PagedDocumentResponse;
import se.sundsvall.document.integration.db.DatabaseHelper;
import se.sundsvall.document.integration.db.model.DocumentDataBinaryEntity;
import se.sundsvall.document.integration.db.model.DocumentDataEntity;
import se.sundsvall.document.integration.db.model.DocumentEntity;
import se.sundsvall.document.integration.db.model.DocumentMetadataEmbeddable;

public class DocumentMapper {

	private DocumentMapper() {}

	/**
	 * API to Database mappings.
	 */

	public static DocumentEntity toDocumentEntity(DocumentCreateRequest documentCreateRequest) {
		return Optional.ofNullable(documentCreateRequest)
			.map(doc -> DocumentEntity.create()
				.withConfidential(doc.isConfidential())
				.withCreatedBy(doc.getCreatedBy())
				.withDescription(doc.getDescription())
				.withMetadata(toDocumentMetadataEmbeddableList(doc.getMetadataList()))
				.withMunicipalityId(doc.getMunicipalityId()))
			.orElse(null);
	}

	public static DocumentEntity toDocumentEntity(DocumentUpdateRequest documentUpdateRequest) {
		return Optional.ofNullable(documentUpdateRequest)
			.map(doc -> DocumentEntity.create()
				.withCreatedBy(doc.getCreatedBy()))
			.orElse(null);
	}

	public static List<DocumentDataEntity> toDocumentDataEntities(List<MultipartFile> multipartFiles, DatabaseHelper databaseHelper) {
		return Optional.ofNullable(multipartFiles)
			.map(files -> files.stream()
				.map(file -> DocumentDataEntity.create()
					.withDocumentDataBinary(toDocumentDataBinaryEntity(file, databaseHelper))
					.withMimeType(file.getContentType())
					.withFileName(file.getOriginalFilename())
					.withFileSizeInBytes(file.getSize()))
				.toList())
			.orElse(null);
	}

	public static List<DocumentDataEntity> toDocumentDataEntities(List<DocumentDataEntity> documentDataEntityList) {
		return Optional.ofNullable(documentDataEntityList)
			.map(list -> list.stream()
				.map(doc -> DocumentDataEntity.create()
					.withMimeType(doc.getMimeType())
					.withFileName(doc.getFileName())
					.withFileSizeInBytes(doc.getFileSizeInBytes())
					.withDocumentDataBinary(toDocumentDataBinaryEntity(doc.getDocumentDataBinary())))
				.toList())
			.orElse(null);
	}

	private static DocumentDataBinaryEntity toDocumentDataBinaryEntity(DocumentDataBinaryEntity documentDataFileEntity) {
		return Optional.ofNullable(documentDataFileEntity)
			.map(docData -> DocumentDataBinaryEntity.create()
				.withBinaryFile(docData.getBinaryFile()))
			.orElse(null);
	}

	private static DocumentDataBinaryEntity toDocumentDataBinaryEntity(MultipartFile multipartFile, DatabaseHelper databaseHelper) {
		return Optional.ofNullable(multipartFile)
			.map(file -> DocumentDataBinaryEntity.create()
				.withBinaryFile(databaseHelper.convertToBlob(file)))
			.orElse(null);
	}

	public static List<DocumentMetadataEmbeddable> toDocumentMetadataEmbeddableList(List<DocumentMetadata> documentMetadataList) {
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

	public static PagedDocumentResponse toPagedDocumentResponse(Page<DocumentEntity> documentEntityPage) {
		return Optional.ofNullable(documentEntityPage)
			.map(page -> PagedDocumentResponse.create()
				.withDocuments(toDocumentList(page.getContent()))
				.withMetaData(PagingMetaData.create()
					.withPage(page.getPageable().getPageNumber())
					.withLimit(page.getPageable().getPageSize())
					.withCount(page.getNumberOfElements())
					.withTotalRecords(page.getTotalElements())
					.withTotalPages(page.getTotalPages())))
			.orElse(null);
	}

	public static Document toDocument(DocumentEntity documentEntity) {
		return Optional.ofNullable(documentEntity)
			.map(docEntity -> Document.create()
				.withConfidential(docEntity.isConfidential())
				.withCreated(docEntity.getCreated())
				.withCreatedBy(docEntity.getCreatedBy())
				.withDescription(docEntity.getDescription())
				.withDocumentData(toDocumentDataList(docEntity.getDocumentData()))
				.withId(docEntity.getId())
				.withMetadataList(toDocumentMetadataList(docEntity.getMetadata()))
				.withMunicipalityId(docEntity.getMunicipalityId())
				.withRegistrationNumber(docEntity.getRegistrationNumber())
				.withRevision(docEntity.getRevision()))
			.orElse(null);
	}

	private static List<DocumentMetadata> toDocumentMetadataList(List<DocumentMetadataEmbeddable> documentMetadataEmbeddableList) {
		return Optional.ofNullable(documentMetadataEmbeddableList).orElse(emptyList()).stream()
			.map(docMetadataEmbeddable -> DocumentMetadata.create()
				.withKey(docMetadataEmbeddable.getKey())
				.withValue(docMetadataEmbeddable.getValue()))
			.toList();
	}

	private static List<DocumentData> toDocumentDataList(List<DocumentDataEntity> documentDataEntityList) {
		return Optional.ofNullable(documentDataEntityList)
			.map(list -> list.stream()
				.map(DocumentMapper::toDocumentData)
				.toList())
			.orElse(null);
	}

	private static DocumentData toDocumentData(DocumentDataEntity documentDataEntity) {
		return Optional.ofNullable(documentDataEntity)
			.map(docDataEntity -> DocumentData.create()
				.withFileName(docDataEntity.getFileName())
				.withFileSizeInBytes(docDataEntity.getFileSizeInBytes())
				.withId(docDataEntity.getId())
				.withMimeType(docDataEntity.getMimeType()))
			.orElse(null);
	}
}
