package se.sundsvall.document.service;

import static generated.se.sundsvall.eventlog.EventType.UPDATE;
import static java.util.Collections.emptyList;
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
import static se.sundsvall.document.service.Constants.TEMPLATE_CONTENT_DISPOSITION_HEADER_VALUE;
import static se.sundsvall.document.service.Constants.TEMPLATE_EVENTLOG_MESSAGE_CONFIDENTIALITY_UPDATED_ON_DOCUMENT;
import static se.sundsvall.document.service.Constants.TEMPLATE_EVENTLOG_MESSAGE_CONFIDENTIALITY_UPDATED_ON_FILE;
import static se.sundsvall.document.service.InclusionFilter.CONFIDENTIAL_AND_PUBLIC;
import static se.sundsvall.document.service.mapper.DocumentMapper.copyDocumentEntity;
import static se.sundsvall.document.service.mapper.DocumentMapper.toConfidentialityEmbeddable;
import static se.sundsvall.document.service.mapper.DocumentMapper.toDocument;
import static se.sundsvall.document.service.mapper.DocumentMapper.toDocumentDataEntities;
import static se.sundsvall.document.service.mapper.DocumentMapper.toDocumentEntity;
import static se.sundsvall.document.service.mapper.DocumentMapper.toInclusionFilter;
import static se.sundsvall.document.service.mapper.DocumentMapper.toPagedDocumentResponse;
import static se.sundsvall.document.service.mapper.EventlogMapper.toEvent;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;

import se.sundsvall.document.api.model.Confidentiality;
import se.sundsvall.document.api.model.ConfidentialityUpdateRequest;
import se.sundsvall.document.api.model.Document;
import se.sundsvall.document.api.model.DocumentCreateRequest;
import se.sundsvall.document.api.model.DocumentDataCreateRequest;
import se.sundsvall.document.api.model.DocumentDataUpdateRequest;
import se.sundsvall.document.api.model.DocumentUpdateRequest;
import se.sundsvall.document.api.model.PagedDocumentResponse;
import se.sundsvall.document.integration.db.DatabaseHelper;
import se.sundsvall.document.integration.db.DocumentRepository;
import se.sundsvall.document.integration.db.model.DocumentDataEntity;
import se.sundsvall.document.integration.eventlog.EventlogClient;
import se.sundsvall.document.integration.eventlog.configuration.EventlogProperties;
import se.sundsvall.document.service.mapper.DocumentMapper;

@Service
@Transactional
public class DocumentService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentService.class);

	private final DatabaseHelper databaseHelper;
	private final DocumentRepository documentRepository;
	private final RegistrationNumberService registrationNumberService;
	private final EventlogClient eventLogClient;
	private final EventlogProperties eventLogProperties;

	public DocumentService(
		DatabaseHelper databaseHelper,
		DocumentRepository documentRepository,
		RegistrationNumberService registrationNumberService,
		EventlogClient eventLogClient,
		EventlogProperties eventLogProperties) {

		this.databaseHelper = databaseHelper;
		this.documentRepository = documentRepository;
		this.registrationNumberService = registrationNumberService;
		this.eventLogClient = eventLogClient;
		this.eventLogProperties = eventLogProperties;
	}

	public Document create(final DocumentCreateRequest documentCreateRequest, final List<MultipartFile> documentFile) {

		final var documentDataEntities = toDocumentDataEntities(documentFile, databaseHelper, documentCreateRequest.getConfidentiality());
		final var registrationNumber = registrationNumberService.generateRegistrationNumber(documentCreateRequest.getMunicipalityId());

		final var documentEntity = toDocumentEntity(documentCreateRequest)
			.withRegistrationNumber(registrationNumber)
			.withDocumentData(documentDataEntities);

		return toDocument(documentRepository.save(documentEntity));
	}

	public Document read(String registrationNumber, boolean includeConfidential) {

		final var documentEntity = documentRepository.findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(registrationNumber, toInclusionFilter(includeConfidential))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERROR_DOCUMENT_BY_REGISTRATION_NUMBER_NOT_FOUND.formatted(registrationNumber)));

		return toDocument(documentEntity);
	}

	public Document read(String registrationNumber, int revision, boolean includeConfidential) {

		final var documentEntity = documentRepository.findByRegistrationNumberAndRevisionAndConfidentialityConfidentialIn(registrationNumber, revision, toInclusionFilter(includeConfidential))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERROR_DOCUMENT_BY_REGISTRATION_NUMBER_AND_REVISION_NOT_FOUND.formatted(registrationNumber, revision)));

		return toDocument(documentEntity);
	}

	public PagedDocumentResponse readAll(String registrationNumber, boolean includeConfidential, Pageable pageable) {
		return toPagedDocumentResponse(documentRepository.findByRegistrationNumberAndConfidentialityConfidentialIn(registrationNumber, toInclusionFilter(includeConfidential), pageable));
	}

	public PagedDocumentResponse search(String query, boolean includeConfidential, Pageable pageable) {
		final var list = toPagedDocumentResponse(documentRepository.search(query, includeConfidential, pageable));

		// Remove all confidential documentData elements.
		list.getDocuments().stream()
			.forEach(document -> document.getDocumentData()
				.removeIf(documentData -> !includeConfidential && Optional.ofNullable(documentData.getConfidentiality()).orElse(Confidentiality.create()).isConfidential()));

		return list;
	}

	public void readFile(String registrationNumber, String documentDataId, boolean includeConfidential, HttpServletResponse response) {

		final var documentEntity = documentRepository.findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(registrationNumber, toInclusionFilter(includeConfidential))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERROR_DOCUMENT_BY_REGISTRATION_NUMBER_NOT_FOUND.formatted(registrationNumber)));

		if (isEmpty(documentEntity.getDocumentData())) {
			throw Problem.valueOf(NOT_FOUND, ERROR_DOCUMENT_FILE_BY_REGISTRATION_NUMBER_NOT_FOUND.formatted(registrationNumber));
		}

		final var documentDataEntity = documentEntity.getDocumentData().stream()
			.filter(docData -> docData.getId().equals(documentDataId))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERROR_DOCUMENT_FILE_BY_ID_NOT_FOUND.formatted(documentDataId)));

		addFileContentToResponse(documentDataEntity, response);
	}

	public void readFile(String registrationNumber, int revision, String documentDataId, boolean includeConfidential, HttpServletResponse response) {

		final var documentEntity = documentRepository.findByRegistrationNumberAndRevisionAndConfidentialityConfidentialIn(registrationNumber, revision, toInclusionFilter(includeConfidential))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERROR_DOCUMENT_BY_REGISTRATION_NUMBER_AND_REVISION_NOT_FOUND.formatted(registrationNumber, revision)));

		if (isEmpty(documentEntity.getDocumentData())) {
			throw Problem.valueOf(NOT_FOUND, ERROR_DOCUMENT_FILE_BY_REGISTRATION_NUMBER_AND_REVISION_NOT_FOUND.formatted(registrationNumber, revision));
		}

		final var documentDataEntity = documentEntity.getDocumentData().stream()
			.filter(docData -> docData.getId().equals(documentDataId))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERROR_DOCUMENT_FILE_BY_ID_NOT_FOUND.formatted(documentDataId)));

		addFileContentToResponse(documentDataEntity, response);
	}

	public Document addFile(String registrationNumber, DocumentDataCreateRequest documentDataCreateRequest, MultipartFile documentFile) {

		final var documentEntity = documentRepository.findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(registrationNumber, CONFIDENTIAL_AND_PUBLIC.getValue())
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERROR_DOCUMENT_BY_REGISTRATION_NUMBER_NOT_FOUND.formatted(registrationNumber)));

		// Add documentData element to existing list.
		final var newDocumentDataEntity = DocumentMapper.toDocumentDataEntity(documentFile, databaseHelper, documentDataCreateRequest.getConfidentiality());
		final var documentDataList = new ArrayList<>(Optional.ofNullable(documentEntity.getDocumentData()).orElse(emptyList()));
		documentDataList.add(newDocumentDataEntity);

		// Do not update existing entity, create a new revision instead.
		final var newDocumentEntity = copyDocumentEntity(documentEntity)
			.withRevision(documentEntity.getRevision() + 1)
			.withCreatedBy(documentDataCreateRequest.getCreatedBy())
			.withDocumentData(documentDataList);

		return toDocument(documentRepository.save(newDocumentEntity));
	}

	public void deleteFile(String registrationNumber, String documentDataId) {

		final var documentEntity = documentRepository.findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(registrationNumber, toInclusionFilter(true))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERROR_DOCUMENT_BY_REGISTRATION_NUMBER_NOT_FOUND.formatted(registrationNumber)));

		if (isEmpty(documentEntity.getDocumentData())) {
			throw Problem.valueOf(NOT_FOUND, ERROR_DOCUMENT_FILE_BY_REGISTRATION_NUMBER_NOT_FOUND.formatted(registrationNumber));
		}

		// Do not update existing entity, create a new revision instead.
		final var newDocumentEntity = copyDocumentEntity(documentEntity)
			.withRevision(documentEntity.getRevision() + 1)
			.withDocumentData(documentEntity.getDocumentData().stream()
				.filter(docDataEntity -> !docDataEntity.getId().equals(documentDataId)) // Create a new documentData list without the "deleted" object.
				.map(DocumentMapper::copyDocumentDataEntity)
				.toList());

		// If size on new list is the same as the old list, nothing was removed in new revision.
		if (documentEntity.getDocumentData().size() == newDocumentEntity.getDocumentData().size()) {
			throw Problem.valueOf(NOT_FOUND, ERROR_DOCUMENT_FILE_BY_ID_NOT_FOUND.formatted(documentDataId));
		}

		documentRepository.save(newDocumentEntity);
	}

	public Document updateFile(String registrationNumber, String documentDataId, boolean includeConfidential, DocumentDataUpdateRequest documentDataUpdateRequest) {

		final var documentEntity = documentRepository.findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(registrationNumber, toInclusionFilter(includeConfidential))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERROR_DOCUMENT_BY_REGISTRATION_NUMBER_NOT_FOUND.formatted(registrationNumber)));

		if (isEmpty(documentEntity.getDocumentData())) {
			throw Problem.valueOf(NOT_FOUND, ERROR_DOCUMENT_FILE_BY_REGISTRATION_NUMBER_NOT_FOUND.formatted(registrationNumber));
		}

		// Update confidentiality settings on documentDataEntity.
		final var documentDataEntity = documentEntity.getDocumentData().stream()
			.filter(docData -> docData.getId().equals(documentDataId))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERROR_DOCUMENT_FILE_BY_ID_NOT_FOUND.formatted(documentDataId)))
			.withConfidentiality(toConfidentialityEmbeddable(documentDataUpdateRequest.getConfidentiality()));
		Optional.ofNullable(documentDataUpdateRequest.getArchive()).ifPresent(documentDataEntity::setArchive);

		// Do not update existing entity, create a new revision instead.
		final var newDocumentEntity = copyDocumentEntity(documentEntity)
			.withRevision(documentEntity.getRevision() + 1);

		// Send info to Eventlog.
		eventLogForDocumentFile(registrationNumber, documentDataEntity.getFileName(), documentDataUpdateRequest);

		return toDocument(documentRepository.save(newDocumentEntity));
	}

	public Document update(String registrationNumber, boolean includeConfidential, DocumentUpdateRequest documentUpdateRequest) {

		final var existingDocumentEntity = documentRepository.findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(registrationNumber, toInclusionFilter(includeConfidential))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERROR_DOCUMENT_BY_REGISTRATION_NUMBER_NOT_FOUND.formatted(registrationNumber)));

		// Do not update existing entity, create a new revision instead.
		final var newDocumentEntity = toDocumentEntity(documentUpdateRequest, existingDocumentEntity);

		return toDocument(documentRepository.save(newDocumentEntity));
	}

	public void updateConfidentiality(String registrationNumber, ConfidentialityUpdateRequest confidentialityUpdateRequest) {

		final var documentEntities = documentRepository.findByRegistrationNumberAndConfidentialityConfidentialIn(registrationNumber, CONFIDENTIAL_AND_PUBLIC.getValue());

		final var newConfidentialitySettings = toConfidentialityEmbeddable(confidentialityUpdateRequest);

		documentEntities.forEach(documentEntity -> {

			// Set confidentiality settings on document-level.
			documentEntity.setConfidentiality(newConfidentialitySettings);

			// Set confidentiality settings on document-data-level.
			Optional.ofNullable(documentEntity.getDocumentData()).orElse(emptyList())
				.forEach(documentDataEntity -> documentDataEntity.setConfidentiality(newConfidentialitySettings));
		});

		// Send info to Eventlog.
		eventLogForDocument(registrationNumber, confidentialityUpdateRequest);

		documentRepository.saveAll(documentEntities);
	}

	private void addFileContentToResponse(DocumentDataEntity documentDataEntity, HttpServletResponse response) {

		try {
			final var file = documentDataEntity.getDocumentDataBinary().getBinaryFile();
			response.addHeader(CONTENT_TYPE, documentDataEntity.getMimeType());
			response.addHeader(CONTENT_DISPOSITION, TEMPLATE_CONTENT_DISPOSITION_HEADER_VALUE.formatted(documentDataEntity.getFileName()));
			response.setContentLength((int) file.length());

			copy(file.getBinaryStream(), response.getOutputStream());
		} catch (SQLException | IOException e) {
			LOGGER.warn(ERROR_DOCUMENT_FILE_BY_REGISTRATION_NUMBER_COULD_NOT_READ.formatted(documentDataEntity.getId()), e);
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, ERROR_DOCUMENT_FILE_BY_REGISTRATION_NUMBER_COULD_NOT_READ.formatted(documentDataEntity.getId()));
		}
	}

	private void eventLogForDocument(String registrationNumber, ConfidentialityUpdateRequest confidentialityUpdateRequest) {
		eventLogClient.createEvent(eventLogProperties.logKeyUuid(), toEvent(
			UPDATE,
			registrationNumber,
			TEMPLATE_EVENTLOG_MESSAGE_CONFIDENTIALITY_UPDATED_ON_DOCUMENT
				.formatted(confidentialityUpdateRequest.getConfidential(), confidentialityUpdateRequest.getLegalCitation(), registrationNumber, confidentialityUpdateRequest.getChangedBy()),
			confidentialityUpdateRequest.getChangedBy()));
	}

	private void eventLogForDocumentFile(String registrationNumber, String fileName, DocumentDataUpdateRequest documentDataUpdateRequest) {

		final var confidentiality = Optional.ofNullable(documentDataUpdateRequest.getConfidentiality()).orElse(Confidentiality.create());

		eventLogClient.createEvent(eventLogProperties.logKeyUuid(), toEvent(
			UPDATE,
			registrationNumber,
			TEMPLATE_EVENTLOG_MESSAGE_CONFIDENTIALITY_UPDATED_ON_FILE.formatted(confidentiality.isConfidential(), confidentiality.getLegalCitation(), registrationNumber, fileName, documentDataUpdateRequest.getCreatedBy()),
			documentDataUpdateRequest.getCreatedBy()));
	}
}
