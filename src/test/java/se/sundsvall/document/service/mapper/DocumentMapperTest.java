package se.sundsvall.document.service.mapper;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mariadb.jdbc.MariaDbBlob;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import se.sundsvall.document.api.model.Document;
import se.sundsvall.document.api.model.DocumentCreateRequest;
import se.sundsvall.document.api.model.DocumentMetadata;
import se.sundsvall.document.api.model.DocumentUpdateRequest;
import se.sundsvall.document.integration.db.DatabaseHelper;
import se.sundsvall.document.integration.db.model.DocumentDataEntity;
import se.sundsvall.document.integration.db.model.DocumentEntity;
import se.sundsvall.document.integration.db.model.DocumentMetadataEmbeddable;

@ExtendWith(MockitoExtension.class)
class DocumentMapperTest {

	private static final OffsetDateTime CREATED = now(systemDefault());
	private static final String CREATED_BY = "createdBy";
	private static final String ID = "id";
	private static final String METADATA_KEY = "key";
	private static final String METADATA_VALUE = "value";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String REGISTRATION_NUMBER = "reistrationNumber";
	private static final int REVISION = 666;

	@Mock
	private DatabaseHelper databaseHelperMock;

	@Test
	void toDocumentEntityFromDocument() {

		// Arrange
		final var document = Document.create()
			.withCreated(CREATED)
			.withCreatedBy(CREATED_BY)
			.withId(ID)
			.withMetadataList(List.of(DocumentMetadata.create().withKey(METADATA_KEY).withValue(METADATA_VALUE)))
			.withMunicipalityId(MUNICIPALITY_ID)
			.withRegistrationNumber(REGISTRATION_NUMBER)
			.withRevision(REVISION);

		// Act
		final var result = DocumentMapper.toDocumentEntity(document);

		// Assert
		assertThat(result)
			.isNotNull()
			.isEqualTo(DocumentEntity.create()
				.withCreated(CREATED)
				.withCreatedBy(CREATED_BY)
				.withId(ID)
				.withMetadata(List.of(DocumentMetadataEmbeddable.create()
					.withKey(METADATA_KEY)
					.withValue(METADATA_VALUE)))
				.withMunicipalityId(MUNICIPALITY_ID)
				.withRegistrationNumber(REGISTRATION_NUMBER)
				.withRevision(REVISION));
	}

	@Test
	void toDocumentEntityFromDocumentWhenInputIsNull() {
		assertThat(DocumentMapper.toDocumentEntity((Document) null)).isNull();
	}

	@Test
	void toDocumentEntityFromDocumentCreateRequest() {

		// Arrange
		final var documentCreateRequest = DocumentCreateRequest.create()
			.withCreatedBy(CREATED_BY)
			.withMetadataList(List.of(DocumentMetadata.create().withKey(METADATA_KEY).withValue(METADATA_VALUE)))
			.withMunicipalityId(MUNICIPALITY_ID);

		// Act
		final var result = DocumentMapper.toDocumentEntity(documentCreateRequest);

		// Assert
		assertThat(result)
			.isNotNull()
			.isEqualTo(DocumentEntity.create()
				.withCreatedBy(CREATED_BY)
				.withMetadata(List.of(DocumentMetadataEmbeddable.create()
					.withKey(METADATA_KEY)
					.withValue(METADATA_VALUE)))
				.withMunicipalityId(MUNICIPALITY_ID));
	}

	@Test
	void toDocumentEntityFromDocumentCreateRequestWhenInputIsNull() {
		assertThat(DocumentMapper.toDocumentEntity((DocumentCreateRequest) null)).isNull();
	}

	@Test
	void toDocumentEntityFromDocumentUpdateRequest() {

		// Arrange
		final var documentUpdateRequest = DocumentUpdateRequest.create()
			.withCreatedBy(CREATED_BY)
			.withMetadataList(List.of(DocumentMetadata.create().withKey(METADATA_KEY).withValue(METADATA_VALUE)));

		// Act
		final var result = DocumentMapper.toDocumentEntity(documentUpdateRequest);

		// Assert
		assertThat(result)
			.isNotNull()
			.isEqualTo(DocumentEntity.create()
				.withCreatedBy(CREATED_BY)
				.withMetadata(List.of(DocumentMetadataEmbeddable.create()
					.withKey(METADATA_KEY)
					.withValue(METADATA_VALUE))));
	}

	@Test
	void toDocumentEntityFromDocumentUpdateRequestWhenInputIsNull() {
		assertThat(DocumentMapper.toDocumentEntity((DocumentUpdateRequest) null)).isNull();
	}

	@Test
	void toDocumentList() {

		// Arrange
		final var documentEntity = DocumentEntity.create()
			.withCreated(CREATED)
			.withCreatedBy(CREATED_BY)
			.withId(ID)
			.withMetadata(List.of(DocumentMetadataEmbeddable.create().withKey(METADATA_KEY).withValue(METADATA_VALUE)))
			.withMunicipalityId(MUNICIPALITY_ID)
			.withRegistrationNumber(REGISTRATION_NUMBER)
			.withRevision(REVISION);

		// Act
		final var result = DocumentMapper.toDocumentList(List.of(documentEntity));

		// Assert
		assertThat(result)
			.hasSize(1)
			.containsExactly(Document.create()
				.withCreated(CREATED)
				.withCreatedBy(CREATED_BY)
				.withId(ID)
				.withMetadataList(List.of(DocumentMetadata.create().withKey(METADATA_KEY).withValue(METADATA_VALUE)))
				.withMunicipalityId(MUNICIPALITY_ID)
				.withRegistrationNumber(REGISTRATION_NUMBER)
				.withRevision(REVISION));
	}

	@Test
	void toDocument() {

		// Arrange
		final var documentEntity = DocumentEntity.create()
			.withCreated(CREATED)
			.withCreatedBy(CREATED_BY)
			.withId(ID)
			.withMetadata(List.of(DocumentMetadataEmbeddable.create().withKey(METADATA_KEY).withValue(METADATA_VALUE)))
			.withMunicipalityId(MUNICIPALITY_ID)
			.withRegistrationNumber(REGISTRATION_NUMBER)
			.withRevision(REVISION);

		// Act
		final var result = DocumentMapper.toDocument(documentEntity);

		// Assert
		assertThat(result)
			.isNotNull()
			.isEqualTo(Document.create()
				.withCreated(CREATED)
				.withCreatedBy(CREATED_BY)
				.withId(ID)
				.withMetadataList(List.of(DocumentMetadata.create().withKey(METADATA_KEY).withValue(METADATA_VALUE)))
				.withMunicipalityId(MUNICIPALITY_ID)
				.withRegistrationNumber(REGISTRATION_NUMBER)
				.withRevision(REVISION));
	}

	@Test
	void toDocumentWhenInputIsNull() {
		assertThat(DocumentMapper.toDocument(null)).isNull();
	}

	@Test
	void toDocumentDataEntityFromMultipart() throws IOException {

		// Arrange
		final var mimeType = "image/png";
		final var file = new File("src/test/resources/files/image.png");
		final var fileName = file.getName();
		final var multipartFile = new MockMultipartFile("file", fileName, mimeType, IOUtils.toByteArray(new FileInputStream(file)));

		when(databaseHelperMock.convertToBlob(multipartFile)).thenReturn(new MariaDbBlob());

		// Act
		final var result = DocumentMapper.toDocumentDataEntity(multipartFile, databaseHelperMock);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getFileName()).isEqualTo(fileName);
		assertThat(result.getMimeType()).isEqualTo(mimeType);
		assertThat(result.getFile()).isNotNull();
		verify(databaseHelperMock).convertToBlob(multipartFile);
	}

	@Test
	void toDocumentDataEntityFromMultipartWhenInputIsNull() throws IOException {

		// Act
		final var result = DocumentMapper.toDocumentDataEntity(null, databaseHelperMock);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	void toDocumentDataEntityFromDocumentDataEntity() throws IOException {

		// Arrange
		final var file = new MariaDbBlob();
		final var fileName = "fileName";
		final var id = "id";
		final var mimeType = "image/png";

		final var source = DocumentDataEntity.create()
			.withFile(file)
			.withFileName(fileName)
			.withId(id)
			.withMimeType(mimeType);

		// Act
		final var result = DocumentMapper.toDocumentDataEntity(source);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getFileName()).isEqualTo(fileName);
		assertThat(result.getMimeType()).isEqualTo(mimeType);
		assertThat(result.getFile()).isEqualTo(file);
		assertThat(result.getId()).isNull();
	}

	@Test
	void toDocumentDataEntityFromDocumentDataEntityWhenInputIsNull() throws IOException {

		// Act
		final var result = DocumentMapper.toDocumentDataEntity(null);

		// Assert
		assertThat(result).isNull();
	}
}
