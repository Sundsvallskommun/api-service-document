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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import org.springframework.mock.web.MockMultipartFile;
import org.zalando.problem.ThrowableProblem;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import se.sundsvall.document.api.model.Document;
import se.sundsvall.document.api.model.DocumentCreateRequest;
import se.sundsvall.document.api.model.DocumentMetadata;
import se.sundsvall.document.api.model.DocumentUpdateRequest;
import se.sundsvall.document.integration.db.DatabaseHelper;
import se.sundsvall.document.integration.db.DocumentRepository;
import se.sundsvall.document.integration.db.model.DocumentDataEntity;
import se.sundsvall.document.integration.db.model.DocumentEntity;
import se.sundsvall.document.integration.db.model.DocumentMetadataEmbeddable;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

	private static final String FILE_NAME = "image.jpg";
	private static final String MIME_TYPE = "image/jpeg";
	private static final OffsetDateTime CREATED = now(systemDefault());
	private static final String CREATED_BY = "User";
	private static final String ID = randomUUID().toString();
	private static final String METADATA_KEY = "key";
	private static final String METADATA_VALUE = "value";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String REGISTRATION_NUMBER = "2023-2281-4";
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

	@InjectMocks
	private DocumentService documentService;

	@Captor
	private ArgumentCaptor<DocumentEntity> documentEntityCaptor;

	@Test
	void create() throws FileNotFoundException, IOException {

		// Arrange
		final var documentCreateRequest = DocumentCreateRequest.create()
			.withCreatedBy(CREATED_BY)
			.withMetadataList(List.of(DocumentMetadata.create().withKey(METADATA_KEY).withValue(METADATA_VALUE)))
			.withMunicipalityId(MUNICIPALITY_ID);

		final var file = new File("src/test/resources/files/image.png");
		final var multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", toByteArray(new FileInputStream(file)));

		when(registrationNumberServiceMock.generateRegistrationNumber(MUNICIPALITY_ID)).thenReturn(REGISTRATION_NUMBER);
		when(documentRepositoryMock.save(any(DocumentEntity.class))).thenReturn(DocumentEntity.create());

		// Act
		final var result = documentService.create(documentCreateRequest, multipartFile);

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
	void readByRegistrationNumber() {

		// Arrange
		when(documentRepositoryMock.findTopByRegistrationNumberOrderByRevisionDesc(REGISTRATION_NUMBER)).thenReturn(Optional.of(createDocumentEntity()));

		// Act
		final var result = documentService.read(REGISTRATION_NUMBER);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getCreated()).isEqualTo(CREATED);
		assertThat(result.getCreatedBy()).isEqualTo(CREATED_BY);
		assertThat(result.getId()).isEqualTo(ID);
		assertThat(result.getMetadataList()).isEqualTo(List.of(DocumentMetadata.create().withKey(METADATA_KEY).withValue(METADATA_VALUE)));
		assertThat(result.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(result.getRegistrationNumber()).isEqualTo(REGISTRATION_NUMBER);
		assertThat(result.getRevision()).isEqualTo(REVISION);

		verify(documentRepositoryMock).findTopByRegistrationNumberOrderByRevisionDesc(REGISTRATION_NUMBER);
	}

	@Test
	void readByRegistrationNumberNotFound() {

		// Arrange
		when(documentRepositoryMock.findTopByRegistrationNumberOrderByRevisionDesc(REGISTRATION_NUMBER)).thenReturn(empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.read(REGISTRATION_NUMBER));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document with registrationNumber: '2023-2281-4' could be found!");

		verify(documentRepositoryMock).findTopByRegistrationNumberOrderByRevisionDesc(REGISTRATION_NUMBER);
	}

	@Test
	void readByRegistrationNumberAndRevision() {

		// Arrange
		when(documentRepositoryMock.findByRegistrationNumberAndRevision(REGISTRATION_NUMBER, REVISION)).thenReturn(Optional.of(createDocumentEntity()));

		// Act
		final var result = documentService.read(REGISTRATION_NUMBER, REVISION);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getCreated()).isEqualTo(CREATED);
		assertThat(result.getCreatedBy()).isEqualTo(CREATED_BY);
		assertThat(result.getId()).isEqualTo(ID);
		assertThat(result.getMetadataList()).isEqualTo(List.of(DocumentMetadata.create().withKey(METADATA_KEY).withValue(METADATA_VALUE)));
		assertThat(result.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(result.getRegistrationNumber()).isEqualTo(REGISTRATION_NUMBER);
		assertThat(result.getRevision()).isEqualTo(REVISION);

		verify(documentRepositoryMock).findByRegistrationNumberAndRevision(REGISTRATION_NUMBER, REVISION);
	}

	@Test
	void readByRegistrationNumberAndRevisionNotFound() {

		// Arrange
		when(documentRepositoryMock.findByRegistrationNumberAndRevision(REGISTRATION_NUMBER, REVISION)).thenReturn(empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.read(REGISTRATION_NUMBER, REVISION));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document with registrationNumber: '2023-2281-4' and revision: '1' could be found!");

		verify(documentRepositoryMock).findByRegistrationNumberAndRevision(REGISTRATION_NUMBER, REVISION);
	}

	@Test
	void readAll() {

		// Arrange
		when(documentRepositoryMock.findByRegistrationNumberOrderByRevisionAsc(REGISTRATION_NUMBER)).thenReturn(List.of(createDocumentEntity()));

		// Act
		final var result = documentService.readAll(REGISTRATION_NUMBER);

		// Assert
		assertThat(result)
			.hasSize(1)
			.extracting(Document::getCreated, Document::getCreatedBy, Document::getId, Document::getMunicipalityId, Document::getRegistrationNumber, Document::getRevision)
			.containsExactly(tuple(CREATED, CREATED_BY, ID, MUNICIPALITY_ID, REGISTRATION_NUMBER, REVISION));

		verify(documentRepositoryMock).findByRegistrationNumberOrderByRevisionAsc(REGISTRATION_NUMBER);
	}

	@Test
	void readAllNotFound() {

		// Arrange
		when(documentRepositoryMock.findByRegistrationNumberOrderByRevisionAsc(REGISTRATION_NUMBER)).thenReturn(emptyList());

		// Act
		final var result = documentService.readAll(REGISTRATION_NUMBER);

		// Assert
		assertThat(result).isEmpty();

		verify(documentRepositoryMock).findByRegistrationNumberOrderByRevisionAsc(REGISTRATION_NUMBER);
	}

	@Test
	void readFileByRegistrationNumber() throws IOException, SQLException {

		// Arrange
		final var documentEntity = createDocumentEntity();

		when(documentRepositoryMock.findTopByRegistrationNumberOrderByRevisionDesc(REGISTRATION_NUMBER)).thenReturn(Optional.of(documentEntity));
		when(httpServletResponseMock.getOutputStream()).thenReturn(servletOutputStreamMock);

		// Act
		documentService.readFile(REGISTRATION_NUMBER, httpServletResponseMock);

		// Assert
		verify(documentRepositoryMock).findTopByRegistrationNumberOrderByRevisionDesc(REGISTRATION_NUMBER);
		verify(httpServletResponseMock).addHeader(CONTENT_TYPE, MIME_TYPE);
		verify(httpServletResponseMock).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"image.jpg\"");
		verify(httpServletResponseMock).setContentLength((int) documentEntity.getDocumentData().getFile().length());
		verify(httpServletResponseMock).getOutputStream();
	}

	@Test
	void readFileByRegistrationNumberNotFound() throws IOException, SQLException {

		// Arrange
		when(documentRepositoryMock.findTopByRegistrationNumberOrderByRevisionDesc(REGISTRATION_NUMBER)).thenReturn(empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.readFile(REGISTRATION_NUMBER, httpServletResponseMock));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document with registrationNumber: '2023-2281-4' could be found!");

		verify(documentRepositoryMock).findTopByRegistrationNumberOrderByRevisionDesc(REGISTRATION_NUMBER);
		verifyNoInteractions(httpServletResponseMock);
	}

	@Test
	void readFileByRegistrationNumberFileContentNotFound() throws IOException, SQLException {

		// Arrange
		final var documentEntity = createDocumentEntity();

		when(documentRepositoryMock.findTopByRegistrationNumberOrderByRevisionDesc(REGISTRATION_NUMBER)).thenReturn(Optional.of(documentEntity.withDocumentData(null)));

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.readFile(REGISTRATION_NUMBER, httpServletResponseMock));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document file content with registrationNumber: '2023-2281-4' could be found!");

		verify(documentRepositoryMock).findTopByRegistrationNumberOrderByRevisionDesc(REGISTRATION_NUMBER);
		verifyNoInteractions(httpServletResponseMock);
	}

	@Test
	void readFileByRegistrationNumberResponseProcessingFailed() throws IOException, SQLException {

		// Arrange
		final var documentEntity = createDocumentEntity();

		when(documentRepositoryMock.findTopByRegistrationNumberOrderByRevisionDesc(REGISTRATION_NUMBER)).thenReturn(Optional.of(documentEntity));
		when(httpServletResponseMock.getOutputStream()).thenThrow(new IOException("An error occured during byte array copy"));

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.readFile(REGISTRATION_NUMBER, httpServletResponseMock));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Internal Server Error: Could not read file content for document data with id 'some-uuid'!");

		verify(documentRepositoryMock).findTopByRegistrationNumberOrderByRevisionDesc(REGISTRATION_NUMBER);
		verify(documentRepositoryMock).findTopByRegistrationNumberOrderByRevisionDesc(REGISTRATION_NUMBER);
		verify(httpServletResponseMock).addHeader(CONTENT_TYPE, MIME_TYPE);
		verify(httpServletResponseMock).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"image.jpg\"");
		verify(httpServletResponseMock).setContentLength((int) documentEntity.getDocumentData().getFile().length());
		verify(httpServletResponseMock).getOutputStream();
	}

	@Test
	void readFileByRegistrationNumberAndRevision() throws IOException, SQLException {

		// Arrange
		final var documentEntity = createDocumentEntity();

		when(documentRepositoryMock.findByRegistrationNumberAndRevision(REGISTRATION_NUMBER, REVISION)).thenReturn(Optional.of(documentEntity));
		when(httpServletResponseMock.getOutputStream()).thenReturn(servletOutputStreamMock);

		// Act
		documentService.readFile(REGISTRATION_NUMBER, REVISION, httpServletResponseMock);

		// Assert
		verify(documentRepositoryMock).findByRegistrationNumberAndRevision(REGISTRATION_NUMBER, REVISION);
		verify(httpServletResponseMock).addHeader(CONTENT_TYPE, MIME_TYPE);
		verify(httpServletResponseMock).addHeader(CONTENT_DISPOSITION, "attachment; filename=\"image.jpg\"");
		verify(httpServletResponseMock).setContentLength((int) documentEntity.getDocumentData().getFile().length());
		verify(httpServletResponseMock).getOutputStream();
	}

	@Test
	void readFileByRegistrationNumberAndRevisionNotFound() throws IOException, SQLException {

		// Arrange
		when(documentRepositoryMock.findByRegistrationNumberAndRevision(REGISTRATION_NUMBER, REVISION)).thenReturn(empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.readFile(REGISTRATION_NUMBER, REVISION, httpServletResponseMock));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document with registrationNumber: '2023-2281-4' and revision: '1' could be found!");

		verify(documentRepositoryMock).findByRegistrationNumberAndRevision(REGISTRATION_NUMBER, REVISION);
		verifyNoInteractions(httpServletResponseMock);
	}

	@Test
	void readFileByRegistrationNumberAndRevisionFileContentNotFound() throws IOException, SQLException {

		// Arrange
		final var documentEntity = createDocumentEntity();

		when(documentRepositoryMock.findByRegistrationNumberAndRevision(REGISTRATION_NUMBER, REVISION)).thenReturn(Optional.of(documentEntity.withDocumentData(null)));

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.readFile(REGISTRATION_NUMBER, REVISION, httpServletResponseMock));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document file content with registrationNumber: '2023-2281-4' and revision: '1' could be found!");

		verify(documentRepositoryMock).findByRegistrationNumberAndRevision(REGISTRATION_NUMBER, REVISION);
		verifyNoInteractions(httpServletResponseMock);
	}

	@Test
	void update() throws FileNotFoundException, IOException {

		// Arrange
		final var existingEntity = createDocumentEntity();
		final var documentUpdateRequest = DocumentUpdateRequest.create()
			.withCreatedBy("changedUser")
			.withMetadataList(List.of(DocumentMetadata.create().withKey("changedKey").withValue("changedValue")));

		final var file = new File("src/test/resources/files/image.png");
		final var multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", toByteArray(new FileInputStream(file)));

		when(documentRepositoryMock.findTopByRegistrationNumberOrderByRevisionDesc(REGISTRATION_NUMBER)).thenReturn(Optional.of(existingEntity));
		when(documentRepositoryMock.save(any(DocumentEntity.class))).thenReturn(DocumentEntity.create());

		// Act
		final var result = documentService.update(REGISTRATION_NUMBER, documentUpdateRequest, multipartFile);

		// Assert
		assertThat(result).isNotNull();

		verify(databaseHelperMock).convertToBlob(multipartFile);
		verify(documentRepositoryMock).save(documentEntityCaptor.capture());
		verifyNoInteractions(registrationNumberServiceMock);

		final var capturedDocumentEntity = documentEntityCaptor.getValue();
		assertThat(capturedDocumentEntity).isNotNull();
		assertThat(capturedDocumentEntity.getCreatedBy()).isEqualTo("changedUser");
		assertThat(capturedDocumentEntity.getMetadata()).isEqualTo(List.of(DocumentMetadataEmbeddable.create().withKey("changedKey").withValue("changedValue")));
		assertThat(capturedDocumentEntity.getMunicipalityId()).isEqualTo(existingEntity.getMunicipalityId());
		assertThat(capturedDocumentEntity.getRegistrationNumber()).isEqualTo(existingEntity.getRegistrationNumber());
	}

	@Test
	void updateNotFound() throws IOException {

		// Arrange
		final var documentUpdateRequest = DocumentUpdateRequest.create()
			.withCreatedBy("changedUser")
			.withMetadataList(List.of(DocumentMetadata.create().withKey("changedKey").withValue("changedValue")));

		final var file = new File("src/test/resources/files/image.png");
		final var multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", toByteArray(new FileInputStream(file)));

		when(documentRepositoryMock.findTopByRegistrationNumberOrderByRevisionDesc(REGISTRATION_NUMBER)).thenReturn(empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> documentService.update(REGISTRATION_NUMBER, documentUpdateRequest, multipartFile));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("Not Found: No document with registrationNumber: '2023-2281-4' could be found!");

		verify(documentRepositoryMock).findTopByRegistrationNumberOrderByRevisionDesc(REGISTRATION_NUMBER);
		verify(documentRepositoryMock, never()).save(any());
		verifyNoInteractions(registrationNumberServiceMock, databaseHelperMock);
	}

	private DocumentEntity createDocumentEntity() {

		try {
			final var documentDataEntity = DocumentDataEntity.create()
				.withId("some-uuid")
				.withFile(new MariaDbBlob(toByteArray(new FileInputStream(new File("src/test/resources/files/image.png")))))
				.withFileName(FILE_NAME)
				.withMimeType(MIME_TYPE);

			return DocumentEntity.create()
				.withCreated(CREATED)
				.withCreatedBy(CREATED_BY)
				.withDocumentData(documentDataEntity)
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
