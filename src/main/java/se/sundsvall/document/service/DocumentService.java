package se.sundsvall.document.service;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.StreamUtils.copy;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.document.service.Constants.ERROR_DOCUMENT_BY_REGISTRATION_NUMBER_AND_REVISION_NOT_FOUND;
import static se.sundsvall.document.service.Constants.ERROR_DOCUMENT_BY_REGISTRATION_NUMBER_NOT_FOUND;
import static se.sundsvall.document.service.Constants.ERROR_DOCUMENT_FILE_BY_ID_NOT_FOUND;
import static se.sundsvall.document.service.Constants.ERROR_DOCUMENT_FILE_BY_REGISTRATION_NUMBER_AND_REVISION_NOT_FOUND;
import static se.sundsvall.document.service.Constants.ERROR_DOCUMENT_FILE_BY_REGISTRATION_NUMBER_COULD_NOT_READ;
import static se.sundsvall.document.service.Constants.ERROR_DOCUMENT_FILE_BY_REGISTRATION_NUMBER_NOT_FOUND;
import static se.sundsvall.document.service.mapper.DocumentMapper.toDocument;
import static se.sundsvall.document.service.mapper.DocumentMapper.toDocumentDataEntities;
import static se.sundsvall.document.service.mapper.DocumentMapper.toDocumentEntity;
import static se.sundsvall.document.service.mapper.DocumentMapper.toPagedDocumentResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;

import jakarta.servlet.http.HttpServletResponse;
import se.sundsvall.document.api.model.Document;
import se.sundsvall.document.api.model.DocumentCreateRequest;
import se.sundsvall.document.api.model.DocumentUpdateRequest;
import se.sundsvall.document.api.model.PagedDocumentResponse;
import se.sundsvall.document.integration.db.DatabaseHelper;
import se.sundsvall.document.integration.db.DocumentRepository;
import se.sundsvall.document.integration.db.model.DocumentDataEntity;
import se.sundsvall.document.service.mapper.DocumentMapper;

@Service
@Transactional
public class DocumentService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentService.class);

	private final DatabaseHelper databaseHelper;
	private final DocumentRepository documentRepository;
	private final RegistrationNumberService registrationNumberService;

	public DocumentService(DatabaseHelper databaseHelper, DocumentRepository documentRepository, RegistrationNumberService registrationNumberService) {
		this.databaseHelper = databaseHelper;
		this.documentRepository = documentRepository;
		this.registrationNumberService = registrationNumberService;
	}

	public Document create(DocumentCreateRequest documentCreateRequest, List<MultipartFile> documentFile) {

		final var documentEntity = toDocumentEntity(documentCreateRequest)
			.withRegistrationNumber(registrationNumberService.generateRegistrationNumber(documentCreateRequest.getMunicipalityId()))
			.withDocumentData(toDocumentDataEntities(documentFile, databaseHelper));

		return toDocument(documentRepository.save(documentEntity));
	}

	public Document read(String registrationNumber, boolean includeConfidential) {

		final var documentEntity = documentRepository.findTopByRegistrationNumberAndConfidentialOrderByRevisionDesc(registrationNumber, includeConfidential)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(ERROR_DOCUMENT_BY_REGISTRATION_NUMBER_NOT_FOUND, registrationNumber)));

		return toDocument(documentEntity);
	}

	public Document read(String registrationNumber, int revision, boolean includeConfidential) {

		final var documentEntity = documentRepository.findByRegistrationNumberAndRevisionAndConfidential(registrationNumber, revision, includeConfidential)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(ERROR_DOCUMENT_BY_REGISTRATION_NUMBER_AND_REVISION_NOT_FOUND, registrationNumber, revision)));

		return toDocument(documentEntity);
	}

	public PagedDocumentResponse readAll(String registrationNumber, boolean includeConfidential, Pageable pageable) {
		return toPagedDocumentResponse(documentRepository.findByRegistrationNumberAndConfidential(registrationNumber, includeConfidential, pageable));
	}

	public PagedDocumentResponse search(String query, boolean includeConfidential, Pageable pageable) {
		return toPagedDocumentResponse(documentRepository.search(query, includeConfidential, pageable));
	}

	public void readFile(String registrationNumber, String documentDataId, boolean includeConfidential, HttpServletResponse response) {

		final var documentEntity = documentRepository.findTopByRegistrationNumberAndConfidentialOrderByRevisionDesc(registrationNumber, includeConfidential)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(ERROR_DOCUMENT_BY_REGISTRATION_NUMBER_NOT_FOUND, registrationNumber)));

		if (isEmpty(documentEntity.getDocumentData())) {
			throw Problem.valueOf(NOT_FOUND, format(ERROR_DOCUMENT_FILE_BY_REGISTRATION_NUMBER_NOT_FOUND, registrationNumber));
		}

		final var documentDataEntity = documentEntity.getDocumentData().stream()
			.filter(docData -> docData.getId().equals(documentDataId))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(ERROR_DOCUMENT_FILE_BY_ID_NOT_FOUND, documentDataId)));

		addFileContentToResponse(documentDataEntity, response);
	}

	public void readFile(String registrationNumber, int revision, String documentDataId, boolean includeConfidential, HttpServletResponse response) {

		final var documentEntity = documentRepository.findByRegistrationNumberAndRevisionAndConfidential(registrationNumber, revision, includeConfidential)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(ERROR_DOCUMENT_BY_REGISTRATION_NUMBER_AND_REVISION_NOT_FOUND, registrationNumber, revision)));

		if (isEmpty(documentEntity.getDocumentData())) {
			throw Problem.valueOf(NOT_FOUND, format(ERROR_DOCUMENT_FILE_BY_REGISTRATION_NUMBER_AND_REVISION_NOT_FOUND, registrationNumber, revision));
		}

		final var documentDataEntity = documentEntity.getDocumentData().stream()
			.filter(docData -> docData.getId().equals(documentDataId))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(ERROR_DOCUMENT_FILE_BY_ID_NOT_FOUND, documentDataId)));

		addFileContentToResponse(documentDataEntity, response);
	}

	public Document update(String registrationNumber, boolean includeConfidential, DocumentUpdateRequest documentUpdateRequest, MultipartFile documentFile) {

		final var existingDocumentEntity = documentRepository.findTopByRegistrationNumberAndConfidentialOrderByRevisionDesc(registrationNumber, includeConfidential)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(ERROR_DOCUMENT_BY_REGISTRATION_NUMBER_NOT_FOUND, registrationNumber)));

		// Do not update existing entity, create a new revision instead.
		final var newDocumentEntity = toDocumentEntity(documentUpdateRequest)
			.withMunicipalityId(existingDocumentEntity.getMunicipalityId())
			.withRegistrationNumber(registrationNumber)
			.withRevision(existingDocumentEntity.getRevision() + 1)
			.withConfidential(Optional.ofNullable(documentUpdateRequest.getConfidential()).orElse(existingDocumentEntity.isConfidential()))
			.withDescription(Optional.ofNullable(documentUpdateRequest.getDescription()).orElse(existingDocumentEntity.getDescription()))
			.withMetadata(Optional.ofNullable(documentUpdateRequest.getMetadataList())
				.map(DocumentMapper::toDocumentMetadataEmbeddableList)
				.orElse(existingDocumentEntity.getMetadata()))
			.withDocumentData(Optional.ofNullable(documentFile)
				.map(file -> toDocumentDataEntities(List.of(documentFile), databaseHelper))
				.orElse(toDocumentDataEntities(existingDocumentEntity.getDocumentData())));

		return toDocument(documentRepository.save(newDocumentEntity));
	}

	private void addFileContentToResponse(DocumentDataEntity documentDataEntity, HttpServletResponse response) {

		try {
			final var file = documentDataEntity.getDocumentDataBinary().getBinaryFile();
			response.addHeader(CONTENT_TYPE, documentDataEntity.getMimeType());
			response.addHeader(CONTENT_DISPOSITION, format("attachment; filename=\"%s\"", documentDataEntity.getFileName()));
			response.setContentLength((int) file.length());

			copy(file.getBinaryStream(), response.getOutputStream());
		} catch (SQLException | IOException e) {
			LOGGER.warn(ERROR_DOCUMENT_FILE_BY_REGISTRATION_NUMBER_COULD_NOT_READ, e);
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, format(ERROR_DOCUMENT_FILE_BY_REGISTRATION_NUMBER_COULD_NOT_READ, documentDataEntity.getId()));
		}
	}
}
