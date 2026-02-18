package se.sundsvall.document.service;

import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.document.api.model.DocumentType;
import se.sundsvall.document.api.model.DocumentTypeCreateRequest;
import se.sundsvall.document.api.model.DocumentTypeUpdateRequest;
import se.sundsvall.document.integration.db.DocumentTypeRepository;
import se.sundsvall.document.service.mapper.DocumentTypeMapper;

import static org.zalando.problem.Status.BAD_REQUEST;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.document.service.mapper.DocumentTypeMapper.toDocumentType;
import static se.sundsvall.document.service.mapper.DocumentTypeMapper.toDocumentTypeEntity;
import static se.sundsvall.document.service.mapper.DocumentTypeMapper.toDocumentTypes;
import static se.sundsvall.document.service.mapper.DocumentTypeMapper.updateDocumentTypeEntity;

@Service
public class DocumentTypeService {
	private static final String CACHE_NAME = "documentTypeCache";
	private static final String ERROR_DOCUMENT_TYPE_NOT_FOUND = "Document type with identifier %s was not found within municipality with id %s";
	private static final String ERROR_DOCUMENT_TYPE_ALREADY_EXISTS = "Document type with identifier %s already exists in municipality with id %s";

	private final DocumentTypeRepository documentTypeRepository;

	public DocumentTypeService(DocumentTypeRepository documentTypeRepository) {
		this.documentTypeRepository = documentTypeRepository;
	}

	@Caching(evict = {
		@CacheEvict(value = CACHE_NAME, key = "{'read', #municipalityId}"),
		@CacheEvict(value = CACHE_NAME, key = "{'read', #municipalityId, #type}")
	})
	public DocumentType create(final String municipalityId, final DocumentTypeCreateRequest documentTypeCreateRequest) {

		if (documentTypeRepository.existsByMunicipalityIdAndType(municipalityId, documentTypeCreateRequest.getType())) {
			throw Problem.valueOf(BAD_REQUEST, ERROR_DOCUMENT_TYPE_ALREADY_EXISTS.formatted(documentTypeCreateRequest.getType().toUpperCase(), municipalityId));
		}

		final var documentTypeEntity = toDocumentTypeEntity(municipalityId, documentTypeCreateRequest);
		return toDocumentType(documentTypeRepository.save(documentTypeEntity));
	}

	@Cacheable(value = CACHE_NAME, key = "{#root.methodName, #municipalityId}")
	public List<DocumentType> read(final String municipalityId) {
		final var matches = documentTypeRepository.findAllByMunicipalityId(municipalityId);
		return toDocumentTypes(matches);
	}

	@Cacheable(value = CACHE_NAME, key = "{#root.methodName, #municipalityId, #type}")
	public DocumentType read(final String municipalityId, final String type) {
		return documentTypeRepository.findByMunicipalityIdAndType(municipalityId, type)
			.map(DocumentTypeMapper::toDocumentType)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERROR_DOCUMENT_TYPE_NOT_FOUND.formatted(type, municipalityId)));
	}

	@Caching(evict = {
		@CacheEvict(value = CACHE_NAME, key = "{'read', #municipalityId}"),
		@CacheEvict(value = CACHE_NAME, key = "{'read', #municipalityId, #type}")
	})
	public DocumentType update(final String municipalityId, final String type, DocumentTypeUpdateRequest documentTypeUpdateRequest) {
		return documentTypeRepository.findByMunicipalityIdAndType(municipalityId, type)
			.map(existingEntity -> updateDocumentTypeEntity(existingEntity, documentTypeUpdateRequest))
			.map(documentTypeRepository::save)
			.map(DocumentTypeMapper::toDocumentType)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERROR_DOCUMENT_TYPE_NOT_FOUND.formatted(type, municipalityId)));
	}

	@Caching(evict = {
		@CacheEvict(value = CACHE_NAME, key = "{'read', #municipalityId}"),
		@CacheEvict(value = CACHE_NAME, key = "{'read', #municipalityId, #type}")
	})
	public void delete(final String municipalityId, final String type) {
		documentTypeRepository.findByMunicipalityIdAndType(municipalityId, type)
			.ifPresentOrElse(documentTypeRepository::delete, () -> {
				throw Problem.valueOf(NOT_FOUND, ERROR_DOCUMENT_TYPE_NOT_FOUND.formatted(type, municipalityId));
			});
	}
}
