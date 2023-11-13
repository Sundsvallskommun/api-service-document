package se.sundsvall.document.service;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.UUID.randomUUID;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

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

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import se.sundsvall.document.api.model.Document;
import se.sundsvall.document.api.model.DocumentCreateRequest;
import se.sundsvall.document.api.model.DocumentMetadata;
import se.sundsvall.document.api.model.DocumentUpdateRequest;
import se.sundsvall.document.integration.db.DatabaseHelper;
import se.sundsvall.document.integration.db.DocumentRepository;
import se.sundsvall.document.integration.db.model.DocumentDataBinaryEntity;
import se.sundsvall.document.integration.db.model.DocumentDataEntity;
import se.sundsvall.document.integration.db.model.DocumentEntity;
import se.sundsvall.document.integration.db.model.DocumentMetadataEmbeddable;

@ExtendWith(MockitoExtension.class)

// TODO: Add tests and verifications for includeConfidential=true. Also add test for update with multiple files (see test createWithMultipleFiles())
class DocumentServiceTest {

	private static final String FILE_NAME = "image.jpg";
	private static final String MIME_TYPE = "image/jpeg";
	private static final OffsetDateTime CREATED = now(systemDefault());
	private static final String CREATED_BY = "User";
	private static final String DESCRIPTION = "Description";
	private static final String ID = randomUUID().toString();
	private static final String METADATA_KEY = "key";
	private static final String METADATA_VALUE = "value";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String REGISTRATION_NUMBER = "2023-2281-4";
	private static final String DOCUMENT_DATA_ID = randomUUID().toString();
	private static final int REVISION = 1;

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

	@Test
	void create() throws IOException {

		// Arrange
		final var documentCreateRequest = DocumentCreateRequest.create()
			.withCreatedBy(CREATED_BY)
			.withMetadataList(List.of(DocumentMetadata.create().withKey(METADATA_KEY).withValue(METADATA_VALUE)))
			.withMunicipalityId(MUNICIPALITY_ID);

		final var file = new File("src/test/resources/files/image.png");
		final var multipartFile = (MultipartFile) new MockMultipartFile("file", file.getName(), "text/plain", toByteArray(new FileInputStream(file)));
		final var multipartFiles = List.of(multipartFile);

		when(registrationNumberServiceMock.generateRegistrationNumber(MUNICIPALITY_ID)).thenReturn(REGISTRATION_NUMBER);
		when(documentRepositoryMock.save(any(DocumentEntity.class))).thenReturn(DocumentEntity.create());

		// Act
		final var result = documentService.create(documentCreateRequest, multipartFiles);

		// Assert
		assertThat(result).isNotNull();

		verify(registrationNumberServiceMock).generateRegistrationNumber(MUNICIPALITY_ID);
		verify(databaseHelperMock).convertToBlob(multipartFile);
		verify(documentRepositoryMock).save(documentEntityCaptor.capture());

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
		final var multipartFiles = List.of(multipartFile1, multipartFile2);

		when(registrationNumberServiceMock.generateRegistrationNumber(MUNICIPALITY_ID)).thenReturn(REGISTRATION_NUMBER);
		when(documentRepositoryMock.save(any(DocumentEntity.class))).thenReturn(DocumentEntity.create());

		// Act
		final var result = documentService.create(documentCreateRequest, multipartFiles);

		// Assert
		assertThat(result).isNotNull();

		verify(registrationNumberServiceMock).generateRegistrationNumber(MUNICIPALITY_ID);
		verify(databaseHelperMock).convertToBlob(multipartFile1);
		verify(databaseHelperMock).convertToBlob(multipartFile2);
		verify(documentRepositoryMock).save(documentEntityCaptor.capture());

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

		when(documentRepositoryMock.findTopByRegistrationNumberAndConfidentialOrderByRevisionDesc(REGISTRATION_NUMBER, includeConfidential)).thenReturn(Optional.of(createDocumentEntity()));

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

		verify(documentRepositoryMock).findTopByRegistrationNumberAndConfidentialOrderByRevisionDesc(REGISTRATION_NUMBER, includeConfidential);
	}

	@Test
	void readByRegistrationNumberNotFound() {

		// Arrange
		final var includeConfidential = false;

		when(documentRepositoryMock.findTopByRegistrationNumberAndConfidentialOrderByRevisionDesc(REGISTRATION_NUMBER, includeConfidential)).thenReturn(empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.read(REGISTRATION_NUMBER, includeConfidential));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document with registrationNumber: '2023-2281-4' could be found!");

		verify(documentRepositoryMock).findTopByRegistrationNumberAndConfidentialOrderByRevisionDesc(REGISTRATION_NUMBER, includeConfidential);
	}

	@Test
	void readByRegistrationNumberAndRevision() {

		// Arrange
		final var includeConfidential = false;

		when(documentRepositoryMock.findByRegistrationNumberAndRevisionAndConfidential(REGISTRATION_NUMBER, REVISION, includeConfidential)).thenReturn(Optional.of(createDocumentEntity()));

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

		verify(documentRepositoryMock).findByRegistrationNumberAndRevisionAndConfidential(REGISTRATION_NUMBER, REVISION, includeConfidential);
	}

	@Test
	void readByRegistrationNumberAndRevisionNotFound() {

		// Arrange
		final var includeConfidential = false;

		when(documentRepositoryMock.findByRegistrationNumberAndRevisionAndConfidential(REGISTRATION_NUMBER, REVISION, includeConfidential)).thenReturn(empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.read(REGISTRATION_NUMBER, REVISION, includeConfidential));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document with registrationNumber: '2023-2281-4' and revision: '1' could be found!");

		verify(documentRepositoryMock).findByRegistrationNumberAndRevisionAndConfidential(REGISTRATION_NUMBER, REVISION, includeConfidential);
	}

	@Test
	void readAll() {

		// Arrange
		final var includeConfidential = false;

		final var pageRequest = PageRequest.of(0, 10, Sort.by(DESC, "revision"));

		when(pageMock.getContent()).thenReturn(List.of(createDocumentEntity()));
		when(pageMock.getPageable()).thenReturn(pageRequest);
		when(documentRepositoryMock.findByRegistrationNumberAndConfidential(REGISTRATION_NUMBER, includeConfidential, pageRequest)).thenReturn(pageMock);

		// Act
		final var result = documentService.readAll(REGISTRATION_NUMBER, includeConfidential, pageRequest);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getDocuments())
			.extracting(Document::getCreated, Document::getCreatedBy, Document::getId, Document::getMunicipalityId, Document::getRegistrationNumber, Document::getRevision)
			.containsExactly(tuple(CREATED, CREATED_BY, ID, MUNICIPALITY_ID, REGISTRATION_NUMBER, REVISION));

		verify(documentRepositoryMock).findByRegistrationNumberAndConfidential(REGISTRATION_NUMBER, includeConfidential, pageRequest);
	}

	@Test
	void readAllNotFound() {

		// Arrange
		final var includeConfidential = false;
		final var pageRequest = PageRequest.of(0, 10, Sort.by(DESC, "revision"));

		when(pageMock.getContent()).thenReturn(emptyList());
		when(pageMock.getPageable()).thenReturn(pageRequest);
		when(documentRepositoryMock.findByRegistrationNumberAndConfidential(REGISTRATION_NUMBER, includeConfidential, pageRequest)).thenReturn(pageMock);

		// Act
		final var result = documentService.readAll(REGISTRATION_NUMBER, includeConfidential, pageRequest);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getDocuments()).isEmpty();

		verify(documentRepositoryMock).findByRegistrationNumberAndConfidential(REGISTRATION_NUMBER, includeConfidential, pageRequest);
	}

	@Test
	void readFileByRegistrationNumber() throws IOException, SQLException {

		// Arrange
		final var includeConfidential = false;
		final var documentEntity = createDocumentEntity();

		when(documentRepositoryMock.findTopByRegistrationNumberAndConfidentialOrderByRevisionDesc(REGISTRATION_NUMBER, includeConfidential)).thenReturn(Optional.of(documentEntity));
		when(httpServletResponseMock.getOutputStream()).thenReturn(servletOutputStreamMock);

		// Act
		documentService.readFile(REGISTRATION_NUMBER, DOCUMENT_DATA_ID, includeConfidential, httpServletResponseMock);

		// Assert
		verify(documentRepositoryMock).findTopByRegistrationNumberAndConfidentialOrderByRevisionDesc(REGISTRATION_NUMBER, includeConfidential);
		verify(httpServletResponseMock).addHeader(CONTENT_TYPE, MIME_TYPE);
		verify(httpServletResponseMock).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"image.jpg\"");
		verify(httpServletResponseMock).setContentLength((int) documentEntity.getDocumentData().get(0).getDocumentDataBinary().getBinaryFile().length());
		verify(httpServletResponseMock).getOutputStream();
	}

	@Test
	void readFileByRegistrationNumberNotFound() {

		// Arrange
		final var includeConfidential = false;

		when(documentRepositoryMock.findTopByRegistrationNumberAndConfidentialOrderByRevisionDesc(REGISTRATION_NUMBER, includeConfidential)).thenReturn(empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.readFile(REGISTRATION_NUMBER, DOCUMENT_DATA_ID, includeConfidential, httpServletResponseMock));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document with registrationNumber: '2023-2281-4' could be found!");

		verify(documentRepositoryMock).findTopByRegistrationNumberAndConfidentialOrderByRevisionDesc(REGISTRATION_NUMBER, includeConfidential);
		verifyNoInteractions(httpServletResponseMock);
	}

	@Test
	void readFileByRegistrationNumberDocumentDataIdNotFound() {

		// Arrange
		final var includeConfidential = false;
		final var documentEntity = createDocumentEntity();

		// Set id to something that wont be found.
		documentEntity.getDocumentData().get(0).setId("Something else");

		when(documentRepositoryMock.findTopByRegistrationNumberAndConfidentialOrderByRevisionDesc(REGISTRATION_NUMBER, includeConfidential)).thenReturn(Optional.of(documentEntity));

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.readFile(REGISTRATION_NUMBER, DOCUMENT_DATA_ID, includeConfidential, httpServletResponseMock));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document data with ID: '" + DOCUMENT_DATA_ID + "' could be found!");

		verify(documentRepositoryMock).findTopByRegistrationNumberAndConfidentialOrderByRevisionDesc(REGISTRATION_NUMBER, includeConfidential);
		verifyNoInteractions(httpServletResponseMock);
	}

	@Test
	void readFileByRegistrationNumberFileContentNotFound() {

		// Arrange
		final var includeConfidential = false;
		final var documentEntity = createDocumentEntity().withDocumentData(null);

		when(documentRepositoryMock.findTopByRegistrationNumberAndConfidentialOrderByRevisionDesc(REGISTRATION_NUMBER, includeConfidential)).thenReturn(Optional.of(documentEntity));

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.readFile(REGISTRATION_NUMBER, DOCUMENT_DATA_ID, includeConfidential, httpServletResponseMock));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document file content with registrationNumber: '2023-2281-4' could be found!");

		verify(documentRepositoryMock).findTopByRegistrationNumberAndConfidentialOrderByRevisionDesc(REGISTRATION_NUMBER, includeConfidential);
		verifyNoInteractions(httpServletResponseMock);
	}

	@Test
	void readFileByRegistrationNumberResponseProcessingFailed() throws IOException, SQLException {

		// Arrange
		final var includeConfidential = false;
		final var documentEntity = createDocumentEntity();

		when(documentRepositoryMock.findTopByRegistrationNumberAndConfidentialOrderByRevisionDesc(REGISTRATION_NUMBER, includeConfidential)).thenReturn(Optional.of(documentEntity));
		when(httpServletResponseMock.getOutputStream()).thenThrow(new IOException("An error occured during byte array copy"));

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.readFile(REGISTRATION_NUMBER, DOCUMENT_DATA_ID, includeConfidential, httpServletResponseMock));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Internal Server Error: Could not read file content for document data with id '" + DOCUMENT_DATA_ID + "'!");

		verify(documentRepositoryMock).findTopByRegistrationNumberAndConfidentialOrderByRevisionDesc(REGISTRATION_NUMBER, includeConfidential);
		verify(httpServletResponseMock).addHeader(CONTENT_TYPE, MIME_TYPE);
		verify(httpServletResponseMock).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"image.jpg\"");
		verify(httpServletResponseMock).setContentLength((int) documentEntity.getDocumentData().get(0).getDocumentDataBinary().getBinaryFile().length());
		verify(httpServletResponseMock).getOutputStream();
	}

	@Test
	void readFileByRegistrationNumberAndRevision() throws IOException, SQLException {

		// Arrange
		final var includeConfidential = false;
		final var documentEntity = createDocumentEntity();

		when(documentRepositoryMock.findByRegistrationNumberAndRevisionAndConfidential(REGISTRATION_NUMBER, REVISION, includeConfidential)).thenReturn(Optional.of(documentEntity));
		when(httpServletResponseMock.getOutputStream()).thenReturn(servletOutputStreamMock);

		// Act
		documentService.readFile(REGISTRATION_NUMBER, REVISION, DOCUMENT_DATA_ID, includeConfidential, httpServletResponseMock);

		// Assert
		verify(documentRepositoryMock).findByRegistrationNumberAndRevisionAndConfidential(REGISTRATION_NUMBER, REVISION, includeConfidential);
		verify(httpServletResponseMock).addHeader(CONTENT_TYPE, MIME_TYPE);
		verify(httpServletResponseMock).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"image.jpg\"");
		verify(httpServletResponseMock).setContentLength((int) documentEntity.getDocumentData().get(0).getDocumentDataBinary().getBinaryFile().length());
		verify(httpServletResponseMock).getOutputStream();
	}

	@Test
	void readFileByRegistrationNumberAndRevisionNotFound() {

		// Arrange
		final var includeConfidential = false;

		when(documentRepositoryMock.findByRegistrationNumberAndRevisionAndConfidential(REGISTRATION_NUMBER, REVISION, includeConfidential)).thenReturn(empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.readFile(REGISTRATION_NUMBER, REVISION, DOCUMENT_DATA_ID, includeConfidential, httpServletResponseMock));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document with registrationNumber: '2023-2281-4' and revision: '1' could be found!");

		verify(documentRepositoryMock).findByRegistrationNumberAndRevisionAndConfidential(REGISTRATION_NUMBER, REVISION, includeConfidential);
		verifyNoInteractions(httpServletResponseMock);
	}

	@Test
	void readFileByRegistrationNumberAndRevisionFileContentNotFound() {

		// Arrange
		final var includeConfidential = false;
		final var documentEntity = createDocumentEntity().withDocumentData(null);

		when(documentRepositoryMock.findByRegistrationNumberAndRevisionAndConfidential(REGISTRATION_NUMBER, REVISION, includeConfidential)).thenReturn(Optional.of(documentEntity));

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.readFile(REGISTRATION_NUMBER, REVISION, DOCUMENT_DATA_ID, includeConfidential, httpServletResponseMock));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document file content with registrationNumber: '2023-2281-4' and revision: '1' could be found!");

		verify(documentRepositoryMock).findByRegistrationNumberAndRevisionAndConfidential(REGISTRATION_NUMBER, REVISION, includeConfidential);
		verifyNoInteractions(httpServletResponseMock);
	}

	@Test
	void readFileByRegistrationNumberAndRevisionDocumentDataIdNotFound() {

		// Arrange
		final var includeConfidential = false;
		final var documentEntity = createDocumentEntity();

		// Set id to something that wont be found.
		documentEntity.getDocumentData().get(0).setId("Something else");

		when(documentRepositoryMock.findByRegistrationNumberAndRevisionAndConfidential(REGISTRATION_NUMBER, REVISION, includeConfidential)).thenReturn(Optional.of(documentEntity));

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.readFile(REGISTRATION_NUMBER, REVISION, DOCUMENT_DATA_ID, includeConfidential, httpServletResponseMock));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document data with ID: '" + DOCUMENT_DATA_ID + "' could be found!");

		verify(documentRepositoryMock).findByRegistrationNumberAndRevisionAndConfidential(REGISTRATION_NUMBER, REVISION, includeConfidential);
		verifyNoInteractions(httpServletResponseMock);
	}

	@Test
	void search() {

		// Arrange
		final var includeConfidential = false;
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

		verify(documentRepositoryMock).search(search, includeConfidential, pageRequest);
	}

	@Test
	void updateAllValues() throws IOException {

		// Arrange
		final var includeConfidential = false;
		final var existingEntity = createDocumentEntity();
		final var documentUpdateRequest = DocumentUpdateRequest.create()
			.withCreatedBy("changedUser")
			.withConfidential(true)
			.withDescription("changedDescription")
			.withMetadataList(List.of(DocumentMetadata.create().withKey("changedKey").withValue("changedValue")));

		final var file = new File("src/test/resources/files/image.png");
		final var multipartFile = (MultipartFile) new MockMultipartFile("file", file.getName(), "text/plain", toByteArray(new FileInputStream(file)));

		when(documentRepositoryMock.findTopByRegistrationNumberAndConfidentialOrderByRevisionDesc(REGISTRATION_NUMBER, includeConfidential)).thenReturn(Optional.of(existingEntity));
		when(documentRepositoryMock.save(any(DocumentEntity.class))).thenReturn(DocumentEntity.create());

		// Act
		final var result = documentService.update(REGISTRATION_NUMBER, includeConfidential, documentUpdateRequest, multipartFile);

		// Assert
		assertThat(result).isNotNull();

		verify(databaseHelperMock).convertToBlob(multipartFile);
		verify(documentRepositoryMock).save(documentEntityCaptor.capture());
		verifyNoInteractions(registrationNumberServiceMock);

		final var capturedDocumentEntity = documentEntityCaptor.getValue();
		assertThat(capturedDocumentEntity).isNotNull();
		assertThat(capturedDocumentEntity.isConfidential()).isTrue();
		assertThat(capturedDocumentEntity.getCreatedBy()).isEqualTo("changedUser");
		assertThat(capturedDocumentEntity.getDescription()).isEqualTo("changedDescription");
		assertThat(capturedDocumentEntity.getMetadata()).isEqualTo(List.of(DocumentMetadataEmbeddable.create().withKey("changedKey").withValue("changedValue")));
		assertThat(capturedDocumentEntity.getMunicipalityId()).isEqualTo(existingEntity.getMunicipalityId());
		assertThat(capturedDocumentEntity.getRegistrationNumber()).isEqualTo(existingEntity.getRegistrationNumber());
	}

	@Test
	void updateOneValue() throws IOException {

		// Arrange
		final var includeConfidential = false;
		final var existingEntity = createDocumentEntity();
		final var documentUpdateRequest = DocumentUpdateRequest.create()
			.withCreatedBy("changedUser")
			.withMetadataList(List.of(DocumentMetadata.create().withKey("changedKey").withValue("changedValue")));

		final var file = new File("src/test/resources/files/image.png");
		final var multipartFile = (MultipartFile) new MockMultipartFile("file", file.getName(), "text/plain", toByteArray(new FileInputStream(file)));

		when(documentRepositoryMock.findTopByRegistrationNumberAndConfidentialOrderByRevisionDesc(REGISTRATION_NUMBER, includeConfidential)).thenReturn(Optional.of(existingEntity));
		when(documentRepositoryMock.save(any(DocumentEntity.class))).thenReturn(DocumentEntity.create());

		// Act
		final var result = documentService.update(REGISTRATION_NUMBER, includeConfidential, documentUpdateRequest, multipartFile);

		// Assert
		assertThat(result).isNotNull();

		verify(databaseHelperMock).convertToBlob(multipartFile);
		verify(documentRepositoryMock).save(documentEntityCaptor.capture());
		verifyNoInteractions(registrationNumberServiceMock);

		final var capturedDocumentEntity = documentEntityCaptor.getValue();
		assertThat(capturedDocumentEntity).isNotNull();
		assertThat(capturedDocumentEntity.isConfidential()).isFalse();
		assertThat(capturedDocumentEntity.getCreatedBy()).isEqualTo("changedUser");
		assertThat(capturedDocumentEntity.getDescription()).isEqualTo(DESCRIPTION);
		assertThat(capturedDocumentEntity.getMetadata()).isEqualTo(List.of(DocumentMetadataEmbeddable.create().withKey("changedKey").withValue("changedValue")));
		assertThat(capturedDocumentEntity.getMunicipalityId()).isEqualTo(existingEntity.getMunicipalityId());
		assertThat(capturedDocumentEntity.getRegistrationNumber()).isEqualTo(existingEntity.getRegistrationNumber());
	}

	@Test
	void updateNotFound() throws IOException {

		// Arrange
		final var includeConfidential = false;
		final var documentUpdateRequest = DocumentUpdateRequest.create()
			.withCreatedBy("changedUser")
			.withMetadataList(List.of(DocumentMetadata.create().withKey("changedKey").withValue("changedValue")));

		final var file = new File("src/test/resources/files/image.png");
		final var multipartFile = (MultipartFile) new MockMultipartFile("file", file.getName(), "text/plain", toByteArray(new FileInputStream(file)));

		when(documentRepositoryMock.findTopByRegistrationNumberAndConfidentialOrderByRevisionDesc(REGISTRATION_NUMBER, includeConfidential)).thenReturn(empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.update(REGISTRATION_NUMBER, includeConfidential, documentUpdateRequest, multipartFile));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document with registrationNumber: '2023-2281-4' could be found!");

		verify(documentRepositoryMock).findTopByRegistrationNumberAndConfidentialOrderByRevisionDesc(REGISTRATION_NUMBER, includeConfidential);
		verify(documentRepositoryMock, never()).save(any());
		verifyNoInteractions(registrationNumberServiceMock, databaseHelperMock);
	}

	private DocumentEntity createDocumentEntity() {

		try {
			final var documentDataEntity = DocumentDataEntity.create()
				.withId(DOCUMENT_DATA_ID)
				.withDocumentDataBinary(DocumentDataBinaryEntity.create().withBinaryFile(new MariaDbBlob(toByteArray(new FileInputStream(new File("src/test/resources/files/image.png"))))))
				.withFileName(FILE_NAME)
				.withMimeType(MIME_TYPE);

			return DocumentEntity.create()
				.withCreated(CREATED)
				.withCreatedBy(CREATED_BY)
				.withConfidential(false)
				.withDescription(DESCRIPTION)
				.withDocumentData(List.of(documentDataEntity))
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
}
