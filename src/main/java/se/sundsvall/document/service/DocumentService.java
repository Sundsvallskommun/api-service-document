package se.sundsvall.document.service;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.util.StreamUtils.copy;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.document.service.Constants.ERROR_DOCUMENT_BY_REGISTRATION_NUMBER_AND_REVISION_NOT_FOUND;
import static se.sundsvall.document.service.Constants.ERROR_DOCUMENT_BY_REGISTRATION_NUMBER_NOT_FOUND;
import static se.sundsvall.document.service.Constants.ERROR_DOCUMENT_FILE_BY_REGISTRATION_NUMBER_AND_REVISION_NOT_FOUND;
import static se.sundsvall.document.service.Constants.ERROR_DOCUMENT_FILE_BY_REGISTRATION_NUMBER_COULD_NOT_READ;
import static se.sundsvall.document.service.Constants.ERROR_DOCUMENT_FILE_BY_REGISTRATION_NUMBER_NOT_FOUND;
import static se.sundsvall.document.service.mapper.DocumentMapper.toDocument;
import static se.sundsvall.document.service.mapper.DocumentMapper.toDocumentDataEntity;
import static se.sundsvall.document.service.mapper.DocumentMapper.toDocumentEntity;
import static se.sundsvall.document.service.mapper.DocumentMapper.toPagedDocumentResponse;

import java.io.IOException;
import java.sql.SQLException;
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

	public Document create(DocumentCreateRequest documentCreateRequest, MultipartFile documentFile) {

		final var documentEntity = toDocumentEntity(documentCreateRequest)
			.withRegistrationNumber(registrationNumberService.generateRegistrationNumber(documentCreateRequest.getMunicipalityId()))
			.withDocumentData(toDocumentDataEntity(documentFile, databaseHelper));

		return toDocument(documentRepository.save(documentEntity));
	}

	public Document read(String registrationNumber) {

		final var documentEntity = documentRepository.findTopByRegistrationNumberOrderByRevisionDesc(registrationNumber)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(ERROR_DOCUMENT_BY_REGISTRATION_NUMBER_NOT_FOUND, registrationNumber)));

		return toDocument(documentEntity);
	}

	public Document read(String registrationNumber, int revision) {

		final var documentEntity = documentRepository.findByRegistrationNumberAndRevision(registrationNumber, revision)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(ERROR_DOCUMENT_BY_REGISTRATION_NUMBER_AND_REVISION_NOT_FOUND, registrationNumber, revision)));

		return toDocument(documentEntity);
	}

	public PagedDocumentResponse readAll(String registrationNumber, Pageable pageable) {
		return toPagedDocumentResponse(documentRepository.findByRegistrationNumber(registrationNumber, pageable));
	}

	public PagedDocumentResponse search(String query, Pageable pageable) {
		return toPagedDocumentResponse(documentRepository.search(query, pageable));
	}

	public void readFile(String registrationNumber, HttpServletResponse response) {

		final var documentEntity = documentRepository.findTopByRegistrationNumberOrderByRevisionDesc(registrationNumber)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(ERROR_DOCUMENT_BY_REGISTRATION_NUMBER_NOT_FOUND, registrationNumber)));

		if (isNull(documentEntity.getDocumentData()) || isNull(documentEntity.getDocumentData().getFile())) {
			throw Problem.valueOf(NOT_FOUND, format(ERROR_DOCUMENT_FILE_BY_REGISTRATION_NUMBER_NOT_FOUND, registrationNumber));
		}

		addFileContentToResponse(documentEntity.getDocumentData(), response);
	}

	public void readFile(String registrationNumber, int revision, HttpServletResponse response) {

		final var documentEntity = documentRepository.findByRegistrationNumberAndRevision(registrationNumber, revision)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(ERROR_DOCUMENT_BY_REGISTRATION_NUMBER_AND_REVISION_NOT_FOUND, registrationNumber, revision)));

		if (isNull(documentEntity.getDocumentData()) || isNull(documentEntity.getDocumentData().getFile())) {
			throw Problem.valueOf(NOT_FOUND, format(ERROR_DOCUMENT_FILE_BY_REGISTRATION_NUMBER_AND_REVISION_NOT_FOUND, registrationNumber, revision));
		}

		addFileContentToResponse(documentEntity.getDocumentData(), response);
	}

	public Document update(String registrationNumber, DocumentUpdateRequest documentUpdateRequest, MultipartFile documentFile) {

		final var existingDocumentEntity = documentRepository.findTopByRegistrationNumberOrderByRevisionDesc(registrationNumber)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, format(ERROR_DOCUMENT_BY_REGISTRATION_NUMBER_NOT_FOUND, registrationNumber)));

		// Do not update existing entity, create a new revision instead.
		final var newDocumentEntity = toDocumentEntity(documentUpdateRequest)
			.withMunicipalityId(existingDocumentEntity.getMunicipalityId())
			.withRegistrationNumber(registrationNumber)
			.withRevision(existingDocumentEntity.getRevision() + 1)
			.withDocumentData(Optional.ofNullable(documentFile)
				.map(file -> toDocumentDataEntity(documentFile, databaseHelper))
				.orElse(toDocumentDataEntity(existingDocumentEntity.getDocumentData())));

		return toDocument(documentRepository.save(newDocumentEntity));
	}

	private void addFileContentToResponse(DocumentDataEntity documentDataEntity, HttpServletResponse response) {

		try {
			final var file = documentDataEntity.getFile();
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
