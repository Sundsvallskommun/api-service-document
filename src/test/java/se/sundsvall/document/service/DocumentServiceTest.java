package se.sundsvall.document.service;

import static generated.se.sundsvall.eventlog.EventType.UPDATE;
import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static se.sundsvall.document.service.InclusionFilter.CONFIDENTIAL_AND_PUBLIC;
import static se.sundsvall.document.service.InclusionFilter.PUBLIC;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mariadb.jdbc.MariaDbBlob;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.ThrowableProblem;

import generated.se.sundsvall.eventlog.Event;
import generated.se.sundsvall.eventlog.Metadata;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import se.sundsvall.document.api.model.ConfidentialityUpdateRequest;
import se.sundsvall.document.api.model.Document;
import se.sundsvall.document.api.model.DocumentCreateRequest;
import se.sundsvall.document.api.model.DocumentDataCreateRequest;
import se.sundsvall.document.api.model.DocumentFiles;
import se.sundsvall.document.api.model.DocumentMetadata;
import se.sundsvall.document.api.model.DocumentUpdateRequest;
import se.sundsvall.document.integration.db.DatabaseHelper;
import se.sundsvall.document.integration.db.DocumentRepository;
import se.sundsvall.document.integration.db.model.ConfidentialityEmbeddable;
import se.sundsvall.document.integration.db.model.DocumentDataBinaryEntity;
import se.sundsvall.document.integration.db.model.DocumentDataEntity;
import se.sundsvall.document.integration.db.model.DocumentEntity;
import se.sundsvall.document.integration.db.model.DocumentMetadataEmbeddable;
import se.sundsvall.document.integration.eventlog.EventlogClient;
import se.sundsvall.document.integration.eventlog.configuration.EventlogProperties;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

	private static final String FILE_NAME = "image.png";
	private static final String MIME_TYPE = "image/png";
	private static final OffsetDateTime CREATED = now(systemDefault());
	private static final boolean CONFIDENTIAL = true;
	private static final String CREATED_BY = "User";
	private static final String DESCRIPTION = "Description";
	private static final String ID = randomUUID().toString();
	private static final String LEGAL_CITATION = "legalCitation";
	private static final String METADATA_KEY = "key";
	private static final String METADATA_VALUE = "value";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String REGISTRATION_NUMBER = "2023-2281-4";
	private static final String DOCUMENT_DATA_ID = randomUUID().toString();
	private static final int REVISION = 1;

	@Mock
	private EventlogClient eventlogClientMock;

	@Mock
	private EventlogProperties eventlogPropertiesMock;

	@Mock
	private DocumentRepository documentRepositoryMock;

	@Mock
	private RegistrationNumberService registrationNumberServiceMock;

	@Mock
	private DatabaseHelper databaseHelperMock;

	@Mock
	private HttpServletResponse httpServletResponseMock;

	@Mock
	private ServletOutputStream servletOutputStreamMock;

	@Mock
	private Page<DocumentEntity> pageMock;

	@InjectMocks
	private DocumentService documentService;

	@Captor
	private ArgumentCaptor<DocumentEntity> documentEntityCaptor;

	@Captor
	private ArgumentCaptor<List<DocumentEntity>> documentEntitiesCaptor;

	@Captor
	private ArgumentCaptor<Event> eventCaptor;

	@Test
	void create() throws IOException {

		// Arrange
		final var documentCreateRequest = DocumentCreateRequest.create()
			.withCreatedBy(CREATED_BY)
			.withMetadataList(List.of(DocumentMetadata.create().withKey(METADATA_KEY).withValue(METADATA_VALUE)))
			.withMunicipalityId(MUNICIPALITY_ID);

		final var file = new File("src/test/resources/files/image.png");
		final var multipartFile = (MultipartFile) new MockMultipartFile("file", file.getName(), "text/plain", toByteArray(new FileInputStream(file)));
		final var documentFiles = DocumentFiles.create().withFiles(List.of(multipartFile));

		when(registrationNumberServiceMock.generateRegistrationNumber(MUNICIPALITY_ID)).thenReturn(REGISTRATION_NUMBER);
		when(documentRepositoryMock.save(any(DocumentEntity.class))).thenReturn(DocumentEntity.create());

		// Act
		final var result = documentService.create(documentCreateRequest, documentFiles);

		// Assert
		assertThat(result).isNotNull();

		verify(registrationNumberServiceMock).generateRegistrationNumber(MUNICIPALITY_ID);
		verify(databaseHelperMock).convertToBlob(multipartFile);
		verify(documentRepositoryMock).save(documentEntityCaptor.capture());
		verifyNoInteractions(eventlogClientMock);

		final var capturedDocumentEntity = documentEntityCaptor.getValue();
		assertThat(capturedDocumentEntity).isNotNull();
		assertThat(capturedDocumentEntity.getCreatedBy()).isEqualTo(CREATED_BY);
		assertThat(capturedDocumentEntity.getMetadata()).isEqualTo(List.of(DocumentMetadataEmbeddable.create().withKey(METADATA_KEY).withValue(METADATA_VALUE)));
		assertThat(capturedDocumentEntity.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(capturedDocumentEntity.getRegistrationNumber()).isEqualTo(REGISTRATION_NUMBER);
	}

	@Test
	void createWithMultipleFiles() throws IOException {

		// Arrange
		final var documentCreateRequest = DocumentCreateRequest.create()
			.withCreatedBy(CREATED_BY)
			.withMetadataList(List.of(DocumentMetadata.create().withKey(METADATA_KEY).withValue(METADATA_VALUE)))
			.withMunicipalityId(MUNICIPALITY_ID);

		final var file1 = new File("src/test/resources/files/image.png");
		final var file2 = new File("src/test/resources/files/readme.txt");
		final var multipartFile1 = (MultipartFile) new MockMultipartFile("file1", file1.getName(), "image/png", toByteArray(new FileInputStream(file1)));
		final var multipartFile2 = (MultipartFile) new MockMultipartFile("file2", file2.getName(), "text/plain", toByteArray(new FileInputStream(file2)));
		final var documentFiles = DocumentFiles.create().withFiles(List.of(multipartFile1, multipartFile2));

		when(registrationNumberServiceMock.generateRegistrationNumber(MUNICIPALITY_ID)).thenReturn(REGISTRATION_NUMBER);
		when(documentRepositoryMock.save(any(DocumentEntity.class))).thenReturn(DocumentEntity.create());

		// Act
		final var result = documentService.create(documentCreateRequest, documentFiles);

		// Assert
		assertThat(result).isNotNull();

		verify(registrationNumberServiceMock).generateRegistrationNumber(MUNICIPALITY_ID);
		verify(databaseHelperMock).convertToBlob(multipartFile1);
		verify(databaseHelperMock).convertToBlob(multipartFile2);
		verify(documentRepositoryMock).save(documentEntityCaptor.capture());
		verifyNoInteractions(eventlogClientMock);

		final var capturedDocumentEntity = documentEntityCaptor.getValue();
		assertThat(capturedDocumentEntity).isNotNull();
		assertThat(capturedDocumentEntity.getDocumentData())
			.hasSize(2)
			.extracting(DocumentDataEntity::getMimeType, DocumentDataEntity::getFileName, DocumentDataEntity::getFileSizeInBytes)
			.containsExactlyInAnyOrder(
				tuple("text/plain", "readme.txt", 17L),
				tuple("image/png", "image.png", 227546L));
		assertThat(capturedDocumentEntity.getCreatedBy()).isEqualTo(CREATED_BY);
		assertThat(capturedDocumentEntity.getMetadata()).isEqualTo(List.of(DocumentMetadataEmbeddable.create().withKey(METADATA_KEY).withValue(METADATA_VALUE)));
		assertThat(capturedDocumentEntity.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(capturedDocumentEntity.getRegistrationNumber()).isEqualTo(REGISTRATION_NUMBER);
	}

	@Test
	void readByRegistrationNumber() {

		// Arrange
		final var includeConfidential = false;

		when(documentRepositoryMock.findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, PUBLIC.getValue())).thenReturn(Optional.of(createDocumentEntity()));

		// Act
		final var result = documentService.read(REGISTRATION_NUMBER, includeConfidential);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getCreated()).isEqualTo(CREATED);
		assertThat(result.getCreatedBy()).isEqualTo(CREATED_BY);
		assertThat(result.getId()).isEqualTo(ID);
		assertThat(result.getMetadataList()).isEqualTo(List.of(DocumentMetadata.create().withKey(METADATA_KEY).withValue(METADATA_VALUE)));
		assertThat(result.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(result.getRegistrationNumber()).isEqualTo(REGISTRATION_NUMBER);
		assertThat(result.getRevision()).isEqualTo(REVISION);

		verify(documentRepositoryMock).findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, PUBLIC.getValue());
		verifyNoInteractions(eventlogClientMock);
	}

	@Test
	void readByRegistrationNumberNotFound() {

		// Arrange
		final var includeConfidential = false;

		when(documentRepositoryMock.findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, PUBLIC.getValue())).thenReturn(empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.read(REGISTRATION_NUMBER, includeConfidential));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document with registrationNumber: '2023-2281-4' could be found!");

		verify(documentRepositoryMock).findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, PUBLIC.getValue());
		verifyNoInteractions(eventlogClientMock);
	}

	@Test
	void readByRegistrationNumberAndRevision() {

		// Arrange
		final var includeConfidential = false;

		when(documentRepositoryMock.findByRegistrationNumberAndRevisionAndConfidentialityConfidentialIn(REGISTRATION_NUMBER, REVISION, PUBLIC.getValue())).thenReturn(Optional.of(createDocumentEntity()));

		// Act
		final var result = documentService.read(REGISTRATION_NUMBER, REVISION, includeConfidential);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getCreated()).isEqualTo(CREATED);
		assertThat(result.getCreatedBy()).isEqualTo(CREATED_BY);
		assertThat(result.getId()).isEqualTo(ID);
		assertThat(result.getMetadataList()).isEqualTo(List.of(DocumentMetadata.create().withKey(METADATA_KEY).withValue(METADATA_VALUE)));
		assertThat(result.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(result.getRegistrationNumber()).isEqualTo(REGISTRATION_NUMBER);
		assertThat(result.getRevision()).isEqualTo(REVISION);

		verify(documentRepositoryMock).findByRegistrationNumberAndRevisionAndConfidentialityConfidentialIn(REGISTRATION_NUMBER, REVISION, PUBLIC.getValue());
		verifyNoInteractions(eventlogClientMock);
	}

	@Test
	void readByRegistrationNumberAndRevisionWhenNotFound() {

		// Arrange
		final var includeConfidential = false;

		when(documentRepositoryMock.findByRegistrationNumberAndRevisionAndConfidentialityConfidentialIn(REGISTRATION_NUMBER, REVISION, PUBLIC.getValue())).thenReturn(empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.read(REGISTRATION_NUMBER, REVISION, includeConfidential));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document with registrationNumber: '2023-2281-4' and revision: '1' could be found!");

		verify(documentRepositoryMock).findByRegistrationNumberAndRevisionAndConfidentialityConfidentialIn(REGISTRATION_NUMBER, REVISION, PUBLIC.getValue());
		verifyNoInteractions(eventlogClientMock);
	}

	@Test
	void readAll() {

		// Arrange
		final var includeConfidential = false;

		final var pageRequest = PageRequest.of(0, 10, Sort.by(DESC, "revision"));

		when(pageMock.getContent()).thenReturn(List.of(createDocumentEntity()));
		when(pageMock.getPageable()).thenReturn(pageRequest);
		when(documentRepositoryMock.findByRegistrationNumberAndConfidentialityConfidentialIn(REGISTRATION_NUMBER, PUBLIC.getValue(), pageRequest)).thenReturn(pageMock);

		// Act
		final var result = documentService.readAll(REGISTRATION_NUMBER, includeConfidential, pageRequest);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getDocuments())
			.extracting(Document::getCreated, Document::getCreatedBy, Document::getId, Document::getMunicipalityId, Document::getRegistrationNumber, Document::getRevision)
			.containsExactly(tuple(CREATED, CREATED_BY, ID, MUNICIPALITY_ID, REGISTRATION_NUMBER, REVISION));

		verify(documentRepositoryMock).findByRegistrationNumberAndConfidentialityConfidentialIn(REGISTRATION_NUMBER, PUBLIC.getValue(), pageRequest);
		verifyNoInteractions(eventlogClientMock);
	}

	@Test
	void readAllNotFound() {

		// Arrange
		final var includeConfidential = false;
		final var pageRequest = PageRequest.of(0, 10, Sort.by(DESC, "revision"));

		when(pageMock.getContent()).thenReturn(emptyList());
		when(pageMock.getPageable()).thenReturn(pageRequest);
		when(documentRepositoryMock.findByRegistrationNumberAndConfidentialityConfidentialIn(REGISTRATION_NUMBER, PUBLIC.getValue(), pageRequest)).thenReturn(pageMock);

		// Act
		final var result = documentService.readAll(REGISTRATION_NUMBER, includeConfidential, pageRequest);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getDocuments()).isEmpty();

		verify(documentRepositoryMock).findByRegistrationNumberAndConfidentialityConfidentialIn(REGISTRATION_NUMBER, PUBLIC.getValue(), pageRequest);
		verifyNoInteractions(eventlogClientMock);
	}

	@Test
	void readFileByRegistrationNumber() throws IOException, SQLException {

		// Arrange
		final var includeConfidential = false;
		final var documentEntity = createDocumentEntity();

		when(documentRepositoryMock.findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, PUBLIC.getValue())).thenReturn(Optional.of(documentEntity));
		when(httpServletResponseMock.getOutputStream()).thenReturn(servletOutputStreamMock);

		// Act
		documentService.readFile(REGISTRATION_NUMBER, DOCUMENT_DATA_ID, includeConfidential, httpServletResponseMock);

		// Assert
		verify(documentRepositoryMock).findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, PUBLIC.getValue());
		verify(httpServletResponseMock).addHeader(CONTENT_TYPE, MIME_TYPE);
		verify(httpServletResponseMock).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"image.png\"");
		verify(httpServletResponseMock).setContentLength((int) documentEntity.getDocumentData().getFirst().getDocumentDataBinary().getBinaryFile().length());
		verify(httpServletResponseMock).getOutputStream();
		verifyNoInteractions(eventlogClientMock);
	}

	@Test
	void readFileByRegistrationNumberWhenNotFound() {

		// Arrange
		final var includeConfidential = false;

		when(documentRepositoryMock.findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, PUBLIC.getValue())).thenReturn(empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.readFile(REGISTRATION_NUMBER, DOCUMENT_DATA_ID, includeConfidential, httpServletResponseMock));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document with registrationNumber: '2023-2281-4' could be found!");

		verify(documentRepositoryMock).findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, PUBLIC.getValue());
		verifyNoInteractions(httpServletResponseMock, eventlogClientMock);
	}

	@Test
	void readFileByRegistrationNumberWhenDocumentDataIdNotFound() {

		// Arrange
		final var includeConfidential = false;
		final var documentEntity = createDocumentEntity();

		// Set id to something that wont be found.
		documentEntity.getDocumentData().getFirst().setId("Something else");

		when(documentRepositoryMock.findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, PUBLIC.getValue())).thenReturn(Optional.of(documentEntity));

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.readFile(REGISTRATION_NUMBER, DOCUMENT_DATA_ID, includeConfidential, httpServletResponseMock));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document file content with ID: '" + DOCUMENT_DATA_ID + "' could be found!");

		verify(documentRepositoryMock).findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, PUBLIC.getValue());
		verifyNoInteractions(httpServletResponseMock, eventlogClientMock);
	}

	@Test
	void readFileByRegistrationNumberWhenFileContentNotFound() {

		// Arrange
		final var includeConfidential = false;
		final var documentEntity = createDocumentEntity().withDocumentData(null);

		when(documentRepositoryMock.findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, PUBLIC.getValue())).thenReturn(Optional.of(documentEntity));

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.readFile(REGISTRATION_NUMBER, DOCUMENT_DATA_ID, includeConfidential, httpServletResponseMock));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document file for registrationNumber: '2023-2281-4' could be found!");

		verify(documentRepositoryMock).findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, PUBLIC.getValue());
		verifyNoInteractions(httpServletResponseMock, eventlogClientMock);
	}

	@Test
	void readFileByRegistrationNumberResponseProcessingFailed() throws IOException, SQLException {

		// Arrange
		final var includeConfidential = false;
		final var documentEntity = createDocumentEntity();

		when(documentRepositoryMock.findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, PUBLIC.getValue())).thenReturn(Optional.of(documentEntity));
		when(httpServletResponseMock.getOutputStream()).thenThrow(new IOException("An error occured during byte array copy"));

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.readFile(REGISTRATION_NUMBER, DOCUMENT_DATA_ID, includeConfidential, httpServletResponseMock));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Internal Server Error: Could not read file content for document data with ID: '" + DOCUMENT_DATA_ID + "'!");

		verify(documentRepositoryMock).findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, PUBLIC.getValue());
		verify(httpServletResponseMock).addHeader(CONTENT_TYPE, MIME_TYPE);
		verify(httpServletResponseMock).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"image.png\"");
		verify(httpServletResponseMock).setContentLength((int) documentEntity.getDocumentData().getFirst().getDocumentDataBinary().getBinaryFile().length());
		verify(httpServletResponseMock).getOutputStream();
		verifyNoInteractions(eventlogClientMock);
	}

	@Test
	void readFileByRegistrationNumberAndRevision() throws IOException, SQLException {

		// Arrange
		final var includeConfidential = false;
		final var documentEntity = createDocumentEntity();

		when(documentRepositoryMock.findByRegistrationNumberAndRevisionAndConfidentialityConfidentialIn(REGISTRATION_NUMBER, REVISION, PUBLIC.getValue())).thenReturn(Optional.of(documentEntity));
		when(httpServletResponseMock.getOutputStream()).thenReturn(servletOutputStreamMock);

		// Act
		documentService.readFile(REGISTRATION_NUMBER, REVISION, DOCUMENT_DATA_ID, includeConfidential, httpServletResponseMock);

		// Assert
		verify(documentRepositoryMock).findByRegistrationNumberAndRevisionAndConfidentialityConfidentialIn(REGISTRATION_NUMBER, REVISION, PUBLIC.getValue());
		verify(httpServletResponseMock).addHeader(CONTENT_TYPE, MIME_TYPE);
		verify(httpServletResponseMock).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"image.png\"");
		verify(httpServletResponseMock).setContentLength((int) documentEntity.getDocumentData().getFirst().getDocumentDataBinary().getBinaryFile().length());
		verify(httpServletResponseMock).getOutputStream();
		verifyNoInteractions(eventlogClientMock);
	}

	@Test
	void readFileByRegistrationNumberAndRevisionWhenNotFound() {

		// Arrange
		final var includeConfidential = false;

		when(documentRepositoryMock.findByRegistrationNumberAndRevisionAndConfidentialityConfidentialIn(REGISTRATION_NUMBER, REVISION, PUBLIC.getValue())).thenReturn(empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.readFile(REGISTRATION_NUMBER, REVISION, DOCUMENT_DATA_ID, includeConfidential, httpServletResponseMock));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document with registrationNumber: '2023-2281-4' and revision: '1' could be found!");

		verify(documentRepositoryMock).findByRegistrationNumberAndRevisionAndConfidentialityConfidentialIn(REGISTRATION_NUMBER, REVISION, PUBLIC.getValue());
		verifyNoInteractions(httpServletResponseMock, eventlogClientMock);
	}

	@Test
	void readFileByRegistrationNumberAndRevisionFileWhenContentNotFound() {

		// Arrange
		final var includeConfidential = false;
		final var documentEntity = createDocumentEntity().withDocumentData(null);

		when(documentRepositoryMock.findByRegistrationNumberAndRevisionAndConfidentialityConfidentialIn(REGISTRATION_NUMBER, REVISION, PUBLIC.getValue())).thenReturn(Optional.of(documentEntity));

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.readFile(REGISTRATION_NUMBER, REVISION, DOCUMENT_DATA_ID, includeConfidential, httpServletResponseMock));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document file content with registrationNumber: '2023-2281-4' and revision: '1' could be found!");

		verify(documentRepositoryMock).findByRegistrationNumberAndRevisionAndConfidentialityConfidentialIn(REGISTRATION_NUMBER, REVISION, PUBLIC.getValue());
		verifyNoInteractions(httpServletResponseMock, eventlogClientMock);
	}

	@Test
	void readFileByRegistrationNumberAndRevisionWhenDocumentDataIdNotFound() {

		// Arrange
		final var includeConfidential = false;
		final var documentEntity = createDocumentEntity();

		// Set id to something that wont be found.
		documentEntity.getDocumentData().get(0).setId("Something else");

		when(documentRepositoryMock.findByRegistrationNumberAndRevisionAndConfidentialityConfidentialIn(REGISTRATION_NUMBER, REVISION, PUBLIC.getValue())).thenReturn(Optional.of(documentEntity));

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.readFile(REGISTRATION_NUMBER, REVISION, DOCUMENT_DATA_ID, includeConfidential, httpServletResponseMock));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document file content with ID: '" + DOCUMENT_DATA_ID + "' could be found!");

		verify(documentRepositoryMock).findByRegistrationNumberAndRevisionAndConfidentialityConfidentialIn(REGISTRATION_NUMBER, REVISION, PUBLIC.getValue());
		verifyNoInteractions(httpServletResponseMock, eventlogClientMock);
	}

	@Test
	void search() {

		// Arrange
		final var includeConfidential = true;
		final var search = "search-string";
		final var pageRequest = PageRequest.of(0, 10, Sort.by(DESC, "revision"));

		when(pageMock.getContent()).thenReturn(List.of(createDocumentEntity()));
		when(pageMock.getPageable()).thenReturn(pageRequest);
		when(documentRepositoryMock.search(any(), anyBoolean(), any())).thenReturn(pageMock);

		// Act
		final var result = documentService.search(search, includeConfidential, pageRequest);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getDocuments())
			.extracting(Document::getCreated, Document::getCreatedBy, Document::getId, Document::getMunicipalityId, Document::getRegistrationNumber, Document::getRevision)
			.containsExactly(tuple(CREATED, CREATED_BY, ID, MUNICIPALITY_ID, REGISTRATION_NUMBER, REVISION));
		assertThat(result.getDocuments().getFirst().getDocumentData()).hasSize(1); // Document contains a confidential documentData element and we are setting includeConfidential=true

		verify(documentRepositoryMock).search(search, includeConfidential, pageRequest);
		verifyNoInteractions(eventlogClientMock);
	}

	@Test
	void update() {

		// Arrange
		final var includeConfidential = false;
		final var existingEntity = createDocumentEntity();
		final var documentUpdateRequest = DocumentUpdateRequest.create()
			.withCreatedBy("changedUser")
			.withDescription("changedDescription")
			.withMetadataList(List.of(DocumentMetadata.create().withKey("changedKey").withValue("changedValue")));

		when(documentRepositoryMock.findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, PUBLIC.getValue())).thenReturn(Optional.of(existingEntity));
		when(documentRepositoryMock.save(any(DocumentEntity.class))).thenReturn(DocumentEntity.create());

		// Act
		final var result = documentService.update(REGISTRATION_NUMBER, includeConfidential, documentUpdateRequest);

		// Assert
		assertThat(result).isNotNull();

		verify(documentRepositoryMock).save(documentEntityCaptor.capture());
		verifyNoInteractions(registrationNumberServiceMock, eventlogClientMock);

		final var capturedDocumentEntity = documentEntityCaptor.getValue();
		assertThat(capturedDocumentEntity).isNotNull();
		assertThat(capturedDocumentEntity.getRevision()).isEqualTo(REVISION + 1);
		assertThat(capturedDocumentEntity.getConfidentiality()).isEqualTo(ConfidentialityEmbeddable.create().withConfidential(CONFIDENTIAL).withLegalCitation(LEGAL_CITATION));
		assertThat(capturedDocumentEntity.getCreatedBy()).isEqualTo("changedUser");
		assertThat(capturedDocumentEntity.getDescription()).isEqualTo("changedDescription");
		assertThat(capturedDocumentEntity.getDocumentData()).hasSize(1).extracting(DocumentDataEntity::getFileName).containsExactly("image.png");
		assertThat(capturedDocumentEntity.getMetadata()).isEqualTo(List.of(DocumentMetadataEmbeddable.create().withKey("changedKey").withValue("changedValue")));
		assertThat(capturedDocumentEntity.getMunicipalityId()).isEqualTo(existingEntity.getMunicipalityId());
		assertThat(capturedDocumentEntity.getRegistrationNumber()).isEqualTo(existingEntity.getRegistrationNumber());
	}

	@Test
	void updateWhenNotFound() {

		// Arrange
		final var includeConfidential = false;
		final var documentUpdateRequest = DocumentUpdateRequest.create()
			.withCreatedBy("changedUser")
			.withMetadataList(List.of(DocumentMetadata.create().withKey("changedKey").withValue("changedValue")));

		when(documentRepositoryMock.findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, PUBLIC.getValue())).thenReturn(empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.update(REGISTRATION_NUMBER, includeConfidential, documentUpdateRequest));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document with registrationNumber: '2023-2281-4' could be found!");

		verify(documentRepositoryMock).findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, PUBLIC.getValue());
		verify(documentRepositoryMock, never()).save(any());
		verifyNoInteractions(registrationNumberServiceMock, databaseHelperMock, eventlogClientMock);
	}

	@Test
	void updateConfidentiality() {

		// Arrange
		final var eventLogKey = UUID.randomUUID().toString();
		final var newConfidentialValue = true;
		final var existingEntities = List.of(createDocumentEntity(), createDocumentEntity().withRevision(REVISION + 1));
		final var confidentialityUpdateRequest = ConfidentialityUpdateRequest.create()
			.withChangedBy(CREATED_BY)
			.withConfidential(newConfidentialValue)
			.withLegalCitation(LEGAL_CITATION);

		when(eventlogPropertiesMock.logKeyUuid()).thenReturn(eventLogKey);
		when(documentRepositoryMock.findByRegistrationNumberAndConfidentialityConfidentialIn(REGISTRATION_NUMBER, CONFIDENTIAL_AND_PUBLIC.getValue())).thenReturn(existingEntities);
		when(documentRepositoryMock.saveAll(any())).thenReturn(existingEntities);

		// Act
		documentService.updateConfidentiality(REGISTRATION_NUMBER, confidentialityUpdateRequest);

		// Assert
		verify(documentRepositoryMock).findByRegistrationNumberAndConfidentialityConfidentialIn(REGISTRATION_NUMBER, CONFIDENTIAL_AND_PUBLIC.getValue());
		verify(documentRepositoryMock).saveAll(documentEntitiesCaptor.capture());
		verify(eventlogClientMock).createEvent(eq(eventLogKey), eventCaptor.capture());
		verifyNoInteractions(registrationNumberServiceMock);

		// Assert captured DocumentEntity-objects.
		final var capturedDocumentEntities = documentEntitiesCaptor.getValue();
		assertThat(capturedDocumentEntities)
			.isNotNull()
			.hasSize(2)
			.extracting(DocumentEntity::getConfidentiality, DocumentEntity::getRegistrationNumber, DocumentEntity::getRevision)
			.containsExactlyInAnyOrder(
				tuple(ConfidentialityEmbeddable.create().withConfidential(newConfidentialValue).withLegalCitation(LEGAL_CITATION), REGISTRATION_NUMBER, REVISION),
				tuple(ConfidentialityEmbeddable.create().withConfidential(newConfidentialValue).withLegalCitation(LEGAL_CITATION), REGISTRATION_NUMBER, REVISION + 1));

		// Assert captured Eventlog-event.
		final var capturedEvent = eventCaptor.getValue();
		assertThat(capturedEvent).isNotNull();
		assertThat(capturedEvent.getExpires()).isCloseTo(now(systemDefault()).plusYears(10), within(2, SECONDS));
		assertThat(capturedEvent.getType()).isEqualTo(UPDATE);
		assertThat(capturedEvent.getMessage()).isEqualTo("Confidentiality flag updated to: 'true' with legal citation: 'legalCitation' for document with registrationNumber: '2023-2281-4'. Action performed by: 'User'");
		assertThat(capturedEvent.getOwner()).isEqualTo("Document");
		assertThat(capturedEvent.getMetadata())
			.extracting(Metadata::getKey, Metadata::getValue)
			.containsExactlyInAnyOrder(
				tuple("RegistrationNumber", REGISTRATION_NUMBER),
				tuple("ExecutedBy", CREATED_BY));
	}

	@Test
	void addFile() throws IOException {

		final var existingEntity = createDocumentEntity();
		final var documentDataCreateRequest = DocumentDataCreateRequest.create()
			.withCreatedBy("changedUser");

		final var file = new File("src/test/resources/files/image2.png");
		final var multipartFile = (MultipartFile) new MockMultipartFile("file", file.getName(), "image/png", toByteArray(new FileInputStream(file)));

		when(documentRepositoryMock.findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, CONFIDENTIAL_AND_PUBLIC.getValue())).thenReturn(Optional.of(existingEntity));
		when(documentRepositoryMock.save(any(DocumentEntity.class))).thenReturn(DocumentEntity.create());

		// Act
		final var result = documentService.addOrReplaceFile(REGISTRATION_NUMBER, documentDataCreateRequest, multipartFile);

		// Assert
		assertThat(result).isNotNull();

		verify(databaseHelperMock).convertToBlob(multipartFile);
		verify(documentRepositoryMock).save(documentEntityCaptor.capture());
		verifyNoInteractions(registrationNumberServiceMock, eventlogClientMock);

		final var capturedDocumentEntity = documentEntityCaptor.getValue();
		assertThat(capturedDocumentEntity).isNotNull();
		assertThat(capturedDocumentEntity.getConfidentiality()).isEqualTo(existingEntity.getConfidentiality());
		assertThat(capturedDocumentEntity.getCreatedBy()).isEqualTo("changedUser");
		assertThat(capturedDocumentEntity.getDescription()).isEqualTo(existingEntity.getDescription());
		assertThat(capturedDocumentEntity.getDocumentData())
			.hasSize(2)
			.extracting(DocumentDataEntity::getFileName)
			.containsExactlyInAnyOrder(
				"image.png",
				"image2.png");
		assertThat(capturedDocumentEntity.getMetadata()).isEqualTo(existingEntity.getMetadata());
		assertThat(capturedDocumentEntity.getMunicipalityId()).isEqualTo(existingEntity.getMunicipalityId());
		assertThat(capturedDocumentEntity.getRegistrationNumber()).isEqualTo(existingEntity.getRegistrationNumber());
	}

	@Test
	void addFileWithSameName() throws IOException {

		final var existingEntity = createDocumentEntity();
		final var documentDataCreateRequest = DocumentDataCreateRequest.create()
			.withCreatedBy("changedUser");

		final var file = new File("src/test/resources/files/image2.png");
		final var multipartFile = (MultipartFile) new MockMultipartFile("file", FILE_NAME, "image/png", toByteArray(new FileInputStream(file))); // Same name as in "existingEntity"

		when(documentRepositoryMock.findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, CONFIDENTIAL_AND_PUBLIC.getValue())).thenReturn(Optional.of(existingEntity));
		when(documentRepositoryMock.save(any(DocumentEntity.class))).thenReturn(DocumentEntity.create());

		// Act
		final var result = documentService.addOrReplaceFile(REGISTRATION_NUMBER, documentDataCreateRequest, multipartFile);

		// Assert
		assertThat(result).isNotNull();

		verify(databaseHelperMock).convertToBlob(multipartFile);
		verify(documentRepositoryMock).save(documentEntityCaptor.capture());
		verifyNoInteractions(registrationNumberServiceMock, eventlogClientMock);

		final var capturedDocumentEntity = documentEntityCaptor.getValue();
		assertThat(capturedDocumentEntity).isNotNull();
		assertThat(capturedDocumentEntity.getConfidentiality()).isEqualTo(existingEntity.getConfidentiality());
		assertThat(capturedDocumentEntity.getCreatedBy()).isEqualTo("changedUser");
		assertThat(capturedDocumentEntity.getDescription()).isEqualTo(existingEntity.getDescription());
		assertThat(capturedDocumentEntity.getDocumentData())
			.hasSize(1)
			.extracting(DocumentDataEntity::getFileName)
			.containsExactlyInAnyOrder("image.png");
		assertThat(capturedDocumentEntity.getMetadata()).isEqualTo(existingEntity.getMetadata());
		assertThat(capturedDocumentEntity.getMunicipalityId()).isEqualTo(existingEntity.getMunicipalityId());
		assertThat(capturedDocumentEntity.getRegistrationNumber()).isEqualTo(existingEntity.getRegistrationNumber());
	}

	@Test
	void addFileWhenNotFound() throws IOException {

		// Arrange
		final var documentDataCreateRequest = DocumentDataCreateRequest.create()
			.withCreatedBy("changedUser");

		final var file = new File("src/test/resources/files/image2.png");
		final var multipartFile = (MultipartFile) new MockMultipartFile("file", file.getName(), "image/png", toByteArray(new FileInputStream(file)));

		when(documentRepositoryMock.findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(any(), any())).thenReturn(Optional.empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.addOrReplaceFile(REGISTRATION_NUMBER, documentDataCreateRequest, multipartFile));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document with registrationNumber: '2023-2281-4' could be found!");

		verify(documentRepositoryMock).findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, CONFIDENTIAL_AND_PUBLIC.getValue());
		verifyNoMoreInteractions(documentRepositoryMock);
		verifyNoInteractions(eventlogClientMock);
	}

	@Test
	void deleteFileByRegistrationNumberAndDocumentDataId() {

		// Arrange
		final var documentEntity = createDocumentEntity();

		when(documentRepositoryMock.findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, CONFIDENTIAL_AND_PUBLIC.getValue())).thenReturn(Optional.of(documentEntity));

		// Act
		documentService.deleteFile(REGISTRATION_NUMBER, DOCUMENT_DATA_ID);

		// Assert
		verify(documentRepositoryMock).findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, CONFIDENTIAL_AND_PUBLIC.getValue());
		verify(documentRepositoryMock).save(documentEntityCaptor.capture());
		verifyNoInteractions(eventlogClientMock);

		final var capturedEntity = documentEntityCaptor.getValue();
		assertThat(capturedEntity).isNotNull();
		assertThat(capturedEntity.getCreatedBy()).isEqualTo(CREATED_BY);
		assertThat(capturedEntity.getDocumentData()).isEmpty(); // The element in the list should be deleted.
		assertThat(capturedEntity.getMetadata()).isEqualTo(List.of(DocumentMetadataEmbeddable.create().withKey(METADATA_KEY).withValue(METADATA_VALUE)));
		assertThat(capturedEntity.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(capturedEntity.getRegistrationNumber()).isEqualTo(REGISTRATION_NUMBER);
		assertThat(capturedEntity.getRevision()).isEqualTo(REVISION + 1);
	}

	@Test
	void deleteFileByRegistrationNumberAndDocumentDataIdWhenNotFound() {

		// Arrange
		when(documentRepositoryMock.findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, CONFIDENTIAL_AND_PUBLIC.getValue())).thenReturn(empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.deleteFile(REGISTRATION_NUMBER, DOCUMENT_DATA_ID));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document with registrationNumber: '2023-2281-4' could be found!");

		verify(documentRepositoryMock).findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, CONFIDENTIAL_AND_PUBLIC.getValue());
		verifyNoMoreInteractions(documentRepositoryMock);
		verifyNoInteractions(eventlogClientMock);
	}

	@Test
	void deleteFileByRegistrationNumberAndDocumentDataIdWhenDocumentDataIsEmpty() {

		// Arrange
		final var documentEntity = createDocumentEntity().withDocumentData(emptyList());

		when(documentRepositoryMock.findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, CONFIDENTIAL_AND_PUBLIC.getValue())).thenReturn(Optional.of(documentEntity));

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.deleteFile(REGISTRATION_NUMBER, DOCUMENT_DATA_ID));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document file for registrationNumber: '2023-2281-4' could be found!");

		verify(documentRepositoryMock).findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, CONFIDENTIAL_AND_PUBLIC.getValue());
		verifyNoMoreInteractions(documentRepositoryMock);
		verifyNoInteractions(eventlogClientMock);
	}

	@Test
	void deleteFileByRegistrationNumberAndDocumentDataIdWhenDocumentDataIdIsNotFound() {

		// Arrange
		final var documentEntity = createDocumentEntity();

		documentEntity.getDocumentData().getFirst().withId("some-id-that-will-not-be-found");

		when(documentRepositoryMock.findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, CONFIDENTIAL_AND_PUBLIC.getValue())).thenReturn(Optional.of(documentEntity));

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.deleteFile(REGISTRATION_NUMBER, DOCUMENT_DATA_ID));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document file content with ID: '" + DOCUMENT_DATA_ID + "' could be found!");

		verify(documentRepositoryMock).findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(REGISTRATION_NUMBER, CONFIDENTIAL_AND_PUBLIC.getValue());
		verifyNoMoreInteractions(documentRepositoryMock);
		verifyNoInteractions(eventlogClientMock);
	}

	private DocumentEntity createDocumentEntity() {

		try {
			return DocumentEntity.create()
				.withCreated(CREATED)
				.withCreatedBy(CREATED_BY)
				.withConfidentiality(ConfidentialityEmbeddable.create()
					.withConfidential(CONFIDENTIAL)
					.withLegalCitation(LEGAL_CITATION))
				.withDescription(DESCRIPTION)
				.withDocumentData(List.of(createDocumentDataEntity()))
				.withId(ID)
				.withMetadata(List.of(DocumentMetadataEmbeddable.create().withKey(METADATA_KEY).withValue(METADATA_VALUE)))
				.withMunicipalityId(MUNICIPALITY_ID)
				.withRegistrationNumber(REGISTRATION_NUMBER)
				.withRevision(REVISION);
		} catch (final Exception e) {
			fail("Entity could not be created!");
		}
		return null;
	}

	private DocumentDataEntity createDocumentDataEntity() {

		try {
			return DocumentDataEntity.create()
				.withId(DOCUMENT_DATA_ID)
				.withDocumentDataBinary(DocumentDataBinaryEntity.create().withBinaryFile(new MariaDbBlob(toByteArray(new FileInputStream(new File("src/test/resources/files/image.png"))))))
				.withFileName(FILE_NAME)
				.withMimeType(MIME_TYPE);

		} catch (final Exception e) {
			fail("Entity could not be created!");
		}
		return null;
	}
}
