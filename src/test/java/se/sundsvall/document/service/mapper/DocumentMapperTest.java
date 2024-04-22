package se.sundsvall.document.service.mapper;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Sort.Direction.ASC;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mariadb.jdbc.MariaDbBlob;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import se.sundsvall.dept44.models.api.paging.PagingMetaData;
import se.sundsvall.document.api.model.Confidentiality;
import se.sundsvall.document.api.model.ConfidentialityUpdateRequest;
import se.sundsvall.document.api.model.Document;
import se.sundsvall.document.api.model.DocumentCreateRequest;
import se.sundsvall.document.api.model.DocumentData;
import se.sundsvall.document.api.model.DocumentFiles;
import se.sundsvall.document.api.model.DocumentMetadata;
import se.sundsvall.document.api.model.DocumentUpdateRequest;
import se.sundsvall.document.integration.db.DatabaseHelper;
import se.sundsvall.document.integration.db.model.ConfidentialityEmbeddable;
import se.sundsvall.document.integration.db.model.DocumentDataBinaryEntity;
import se.sundsvall.document.integration.db.model.DocumentDataEntity;
import se.sundsvall.document.integration.db.model.DocumentEntity;
import se.sundsvall.document.integration.db.model.DocumentMetadataEmbeddable;

@ExtendWith(MockitoExtension.class)
class DocumentMapperTest {

	private static final boolean ARCHIVE = true;
	private static final boolean CONFIDENTIAL = true;
	private static final String DESCRIPTION = "Description";
	private static final OffsetDateTime CREATED = now(systemDefault());
	private static final String CREATED_BY = "createdBy";
	private static final String FILE_1_NAME = "filename1.png";
	private static final String FILE_2_NAME = "filename2.txt";
	private static final long FILE_1_SIZE_IN_BYTES = 1000;
	private static final long FILE_2_SIZE_IN_BYTES = 2000;
	private static final String LEGAL_CITATION = "legalCitation";
	private static final String MIME_TYPE_1 = "image/png";
	private static final String MIME_TYPE_2 = "text/plain";
	private static final String ID = "id";
	private static final String METADATA_KEY = "key";
	private static final String METADATA_VALUE = "value";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String REGISTRATION_NUMBER = "reistrationNumber";
	private static final int REVISION = 666;

	@Mock
	private DatabaseHelper databaseHelperMock;

	@Test
	void toDocumentEntityFromDocumentCreateRequest() {

		// Arrange
		final var documentCreateRequest = DocumentCreateRequest.create()
			.withConfidentiality(Confidentiality.create()
				.withConfidential(CONFIDENTIAL)
				.withLegalCitation(LEGAL_CITATION))
			.withCreatedBy(CREATED_BY)
			.withDescription(DESCRIPTION)
			.withMetadataList(List.of(DocumentMetadata.create()
				.withKey(METADATA_KEY)
				.withValue(METADATA_VALUE)))
			.withMunicipalityId(MUNICIPALITY_ID);

		// Act
		final var result = DocumentMapper.toDocumentEntity(documentCreateRequest);

		// Assert
		assertThat(result)
			.isNotNull()
			.isEqualTo(DocumentEntity.create()
				.withConfidentiality(ConfidentialityEmbeddable.create()
					.withConfidential(CONFIDENTIAL)
					.withLegalCitation(LEGAL_CITATION))
				.withCreatedBy(CREATED_BY)
				.withDescription(DESCRIPTION)
				.withMetadata(List.of(DocumentMetadataEmbeddable.create()
					.withKey(METADATA_KEY)
					.withValue(METADATA_VALUE)))
				.withMunicipalityId(MUNICIPALITY_ID));
	}

	@Test
	void toDocumentEntityFromDocumentCreateRequestWhenInputIsNull() {
		assertThat(DocumentMapper.toDocumentEntity((DocumentCreateRequest) null)).isNull();
	}

	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	@NullSource
	void toDocumentEntityFromDocumentUpdateRequest(Boolean archive) throws IOException {

		// Arrange
		final var blob = new MariaDbBlob();
		final var mimeType = "image/png";

		final var file1 = new File("src/test/resources/files/image.png");
		final var fileName1 = "image1.png";
		final var multipartFile1 = (MultipartFile) new MockMultipartFile("file", fileName1, mimeType, toByteArray(new FileInputStream(file1)));

		final var file2 = new File("src/test/resources/files/image.png");
		final var fileName2 = "image2.png";
		final var multipartFile2 = (MultipartFile) new MockMultipartFile("file", fileName2, mimeType, toByteArray(new FileInputStream(file2)));

		List.of(multipartFile1, multipartFile2);

		final var documentUpdateRequest = DocumentUpdateRequest.create()
			.withArchive(archive)
			.withCreatedBy("Updated user")
			.withDescription("Updated text")
			.withMetadataList(List.of(DocumentMetadata.create()
				.withKey("Updated-key")
				.withValue("Updated-value")));

		final var existingDocumentEntity = DocumentEntity.create()
			.withArchive(ARCHIVE)
			.withConfidentiality(ConfidentialityEmbeddable.create()
				.withConfidential(CONFIDENTIAL)
				.withLegalCitation(LEGAL_CITATION))
			.withCreatedBy(CREATED_BY)
			.withDescription(DESCRIPTION)
			.withDocumentData(List.of(
				DocumentDataEntity.create()
					.withFileName(fileName1)
					.withFileSizeInBytes(file1.length())
					.withMimeType(mimeType)
					.withDocumentDataBinary(DocumentDataBinaryEntity.create()
						.withBinaryFile(blob)),
				DocumentDataEntity.create()
					.withFileName(fileName2)
					.withFileSizeInBytes(file2.length())
					.withMimeType(mimeType)
					.withDocumentDataBinary(DocumentDataBinaryEntity.create()
						.withBinaryFile(blob))))
			.withMetadata(List.of(DocumentMetadataEmbeddable.create()
				.withKey(METADATA_KEY)
				.withValue(METADATA_VALUE)))
			.withMunicipalityId(MUNICIPALITY_ID)
			.withRegistrationNumber(REGISTRATION_NUMBER)
			.withRevision(REVISION);

		// Act
		final var result = DocumentMapper.toDocumentEntity(documentUpdateRequest, existingDocumentEntity);

		// Assert
		assertThat(result)
			.isNotNull()
			.isEqualTo(DocumentEntity.create()
				.withArchive(archive == null ? existingDocumentEntity.isArchive() : archive)
				.withConfidentiality(ConfidentialityEmbeddable.create()
					.withConfidential(CONFIDENTIAL)
					.withLegalCitation(LEGAL_CITATION))
				.withCreatedBy("Updated user")
				.withDescription("Updated text")
				.withDocumentData(List.of(
					DocumentDataEntity.create()
						.withFileName(fileName1)
						.withFileSizeInBytes(file1.length())
						.withMimeType(mimeType)
						.withDocumentDataBinary(DocumentDataBinaryEntity.create()
							.withBinaryFile(blob)),
					DocumentDataEntity.create()
						.withFileName(fileName2)
						.withFileSizeInBytes(file2.length())
						.withMimeType(mimeType)
						.withDocumentDataBinary(DocumentDataBinaryEntity.create()
							.withBinaryFile(blob))))
				.withMetadata(List.of(DocumentMetadataEmbeddable.create()
					.withKey("Updated-key")
					.withValue("Updated-value")))
				.withMunicipalityId(MUNICIPALITY_ID)
				.withRegistrationNumber(REGISTRATION_NUMBER)
				.withRevision(REVISION + 1));
	}

	@Test
	void toDocumentEntityFromDocumentUpdateRequestWhenInputIsNull() {
		assertThat(DocumentMapper.toDocumentEntity(DocumentUpdateRequest.create(), null)).isNull();
		assertThat(DocumentMapper.toDocumentEntity(null, DocumentEntity.create())).isNull();
	}

	@Test
	void toConfidentialityEmbeddableFromConfidentiality() {

		// Arrange
		final var confidentiality = Confidentiality.create()
			.withConfidential(CONFIDENTIAL)
			.withLegalCitation(LEGAL_CITATION);

		// Act
		final var result = DocumentMapper.toConfidentialityEmbeddable(confidentiality);

		// Assert
		assertThat(result)
			.isNotNull()
			.isEqualTo(ConfidentialityEmbeddable.create()
				.withConfidential(CONFIDENTIAL)
				.withLegalCitation(LEGAL_CITATION));
	}

	@Test
	void toConfidentialityEmbeddableFromConfidentialityWhenInputIsNull() {

		// Act
		final var result = DocumentMapper.toConfidentialityEmbeddable((Confidentiality) null);

		// Assert
		assertThat(result)
			.isNotNull()
			.isEqualTo(ConfidentialityEmbeddable.create()
				.withConfidential(false)
				.withLegalCitation(null));
	}

	@Test
	void toConfidentialityEmbeddableFromConfidentialityUpdateRequest() {

		// Arrange
		final var confidentiality = ConfidentialityUpdateRequest.create()
			.withConfidential(CONFIDENTIAL)
			.withLegalCitation(LEGAL_CITATION)
			.withChangedBy(CREATED_BY);

		// Act
		final var result = DocumentMapper.toConfidentialityEmbeddable(confidentiality);

		// Assert
		assertThat(result)
			.isNotNull()
			.isEqualTo(ConfidentialityEmbeddable.create()
				.withConfidential(CONFIDENTIAL)
				.withLegalCitation(LEGAL_CITATION));
	}

	@Test
	void toConfidentialityEmbeddableFromConfidentialityUpdateRequestWhenInputIsNull() {

		// Act
		final var result = DocumentMapper.toConfidentialityEmbeddable((ConfidentialityUpdateRequest) null);

		// Assert
		assertThat(result)
			.isNotNull()
			.isEqualTo(ConfidentialityEmbeddable.create()
				.withConfidential(false)
				.withLegalCitation(null));
	}

	@Test
	void toConfidentiality() {

		// Arrange
		final var confidentialityEmbeddable = ConfidentialityEmbeddable.create()
			.withConfidential(CONFIDENTIAL)
			.withLegalCitation(LEGAL_CITATION);

		// Act
		final var result = DocumentMapper.toConfidentiality(confidentialityEmbeddable);

		// Assert
		assertThat(result)
			.isNotNull()
			.isEqualTo(Confidentiality.create()
				.withConfidential(CONFIDENTIAL)
				.withLegalCitation(LEGAL_CITATION));
	}

	@Test
	void toConfidentialityWhenInputIsNull() {

		// Act
		final var result = DocumentMapper.toConfidentiality(null);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	void toDocumentList() {

		// Arrange
		final var documentEntity = DocumentEntity.create()
			.withArchive(ARCHIVE)
			.withConfidentiality(ConfidentialityEmbeddable.create()
				.withConfidential(CONFIDENTIAL)
				.withLegalCitation(LEGAL_CITATION))
			.withCreated(CREATED)
			.withCreatedBy(CREATED_BY)
			.withDescription(DESCRIPTION)
			.withDocumentData(List.of(
				DocumentDataEntity.create()
					.withFileName(FILE_1_NAME)
					.withFileSizeInBytes(FILE_1_SIZE_IN_BYTES)
					.withId(ID)
					.withMimeType(MIME_TYPE_1),
				DocumentDataEntity.create()
					.withFileName(FILE_2_NAME)
					.withFileSizeInBytes(FILE_2_SIZE_IN_BYTES)
					.withId(ID)
					.withMimeType(MIME_TYPE_2)))
			.withId(ID)
			.withMetadata(List.of(DocumentMetadataEmbeddable.create()
				.withKey(METADATA_KEY)
				.withValue(METADATA_VALUE)))
			.withMunicipalityId(MUNICIPALITY_ID)
			.withRegistrationNumber(REGISTRATION_NUMBER)
			.withRevision(REVISION);

		// Act
		final var result = DocumentMapper.toDocumentList(List.of(documentEntity));

		// Assert
		assertThat(result)
			.hasSize(1)
			.containsExactly(Document.create()
				.withArchive(ARCHIVE)
				.withConfidentiality(Confidentiality.create()
					.withConfidential(CONFIDENTIAL)
					.withLegalCitation(LEGAL_CITATION))
				.withCreated(CREATED)
				.withCreatedBy(CREATED_BY)
				.withDescription(DESCRIPTION)
				.withDocumentData(List.of(
					DocumentData.create()
						.withFileName(FILE_1_NAME)
						.withFileSizeInBytes(FILE_1_SIZE_IN_BYTES)
						.withId(ID)
						.withMimeType(MIME_TYPE_1),
					DocumentData.create()
						.withFileName(FILE_2_NAME)
						.withFileSizeInBytes(FILE_2_SIZE_IN_BYTES)
						.withId(ID)
						.withMimeType(MIME_TYPE_2)))
				.withId(ID)
				.withMetadataList(List.of(DocumentMetadata.create()
					.withKey(METADATA_KEY)
					.withValue(METADATA_VALUE)))
				.withMunicipalityId(MUNICIPALITY_ID)
				.withRegistrationNumber(REGISTRATION_NUMBER)
				.withRevision(REVISION));
	}

	@Test
	void toDocumentListWhenInputIsNull() {
		assertThat(DocumentMapper.toDocumentList(null)).isEmpty();
	}

	@Test
	void toDocument() {

		// Arrange
		final var documentEntity = DocumentEntity.create()
			.withArchive(ARCHIVE)
			.withConfidentiality(ConfidentialityEmbeddable.create()
				.withConfidential(CONFIDENTIAL)
				.withLegalCitation(LEGAL_CITATION))
			.withCreated(CREATED)
			.withCreatedBy(CREATED_BY)
			.withDescription(DESCRIPTION)
			.withDocumentData(List.of(
				DocumentDataEntity.create()
					.withFileName(FILE_1_NAME)
					.withFileSizeInBytes(FILE_1_SIZE_IN_BYTES)
					.withId(ID)
					.withMimeType(MIME_TYPE_1),
				DocumentDataEntity.create()
					.withFileName(FILE_2_NAME)
					.withFileSizeInBytes(FILE_2_SIZE_IN_BYTES)
					.withId(ID)
					.withMimeType(MIME_TYPE_2)))
			.withId(ID)
			.withMetadata(List.of(DocumentMetadataEmbeddable.create()
				.withKey(METADATA_KEY)
				.withValue(METADATA_VALUE)))
			.withMunicipalityId(MUNICIPALITY_ID)
			.withRegistrationNumber(REGISTRATION_NUMBER)
			.withRevision(REVISION);

		// Act
		final var result = DocumentMapper.toDocument(documentEntity);

		// Assert
		assertThat(result)
			.isNotNull()
			.isEqualTo(Document.create()
				.withArchive(ARCHIVE)
				.withConfidentiality(Confidentiality.create()
					.withConfidential(CONFIDENTIAL)
					.withLegalCitation(LEGAL_CITATION))
				.withCreated(CREATED)
				.withCreatedBy(CREATED_BY)
				.withDescription(DESCRIPTION)
				.withDocumentData(List.of(
					DocumentData.create()
						.withFileName(FILE_1_NAME)
						.withFileSizeInBytes(FILE_1_SIZE_IN_BYTES)
						.withId(ID)
						.withMimeType(MIME_TYPE_1),
					DocumentData.create()
						.withFileName(FILE_2_NAME)
						.withFileSizeInBytes(FILE_2_SIZE_IN_BYTES)
						.withId(ID)
						.withMimeType(MIME_TYPE_2)))
				.withId(ID)
				.withMetadataList(List.of(DocumentMetadata.create()
					.withKey(METADATA_KEY)
					.withValue(METADATA_VALUE)))
				.withMunicipalityId(MUNICIPALITY_ID)
				.withRegistrationNumber(REGISTRATION_NUMBER)
				.withRevision(REVISION));
	}

	@Test
	void toDocumentWhenInputIsNull() {
		assertThat(DocumentMapper.toDocument(null)).isNull();
	}

	@Test
	void toDocumentDataEntitiesFromMultipart() throws IOException {

		// Arrange
		final var blob = new MariaDbBlob();
		final var mimeType = "image/png";
		final var file = new File("src/test/resources/files/image.png");
		final var fileName = file.getName();
		final var multipartFile = (MultipartFile) new MockMultipartFile("file", fileName, mimeType, toByteArray(new FileInputStream(file)));
		final var documents = DocumentFiles.create().withFiles(List.of(multipartFile));

		when(databaseHelperMock.convertToBlob(multipartFile)).thenReturn(blob);

		// Act
		final var result = DocumentMapper.toDocumentDataEntities(documents, databaseHelperMock);

		// Assert
		assertThat(result)
			.isNotNull()
			.isNotEmpty()
			.extracting(
				DocumentDataEntity::getFileName,
				DocumentDataEntity::getMimeType,
				DocumentDataEntity::getFileSizeInBytes,
				DocumentDataEntity::getDocumentDataBinary)
			.containsExactly(tuple(
				fileName,
				mimeType,
				file.length(),
				DocumentDataBinaryEntity.create().withBinaryFile(blob)));

		verify(databaseHelperMock).convertToBlob(multipartFile);
	}

	@Test
	void toDocumentDataEntitiesFromMultipartWhenInputIsNull() throws IOException {

		// Act
		final var result = DocumentMapper.toDocumentDataEntities(null, databaseHelperMock);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	void copyDocumentEntity() {

		// Arrange
		final var documentEntity = DocumentEntity.create()
			.withArchive(ARCHIVE)
			.withConfidentiality(ConfidentialityEmbeddable.create()
				.withConfidential(CONFIDENTIAL)
				.withLegalCitation(LEGAL_CITATION))
			.withCreated(CREATED)
			.withCreatedBy(CREATED_BY)
			.withDescription(DESCRIPTION)
			.withDocumentData(List.of(
				DocumentDataEntity.create()
					.withFileName(FILE_1_NAME)
					.withFileSizeInBytes(FILE_1_SIZE_IN_BYTES)
					.withId(ID)
					.withMimeType(MIME_TYPE_1),
				DocumentDataEntity.create()
					.withFileName(FILE_2_NAME)
					.withFileSizeInBytes(FILE_2_SIZE_IN_BYTES)
					.withId(ID)
					.withMimeType(MIME_TYPE_2)))
			.withId(ID)
			.withMetadata(List.of(DocumentMetadataEmbeddable.create()
				.withKey(METADATA_KEY)
				.withValue(METADATA_VALUE)))
			.withMunicipalityId(MUNICIPALITY_ID)
			.withRegistrationNumber(REGISTRATION_NUMBER)
			.withRevision(REVISION);

		// Act
		final var result = DocumentMapper.copyDocumentEntity(documentEntity);

		// Assert
		assertThat(result)
			.isNotNull()
			.isNotSameAs(documentEntity)
			.isEqualTo(DocumentEntity.create()
				.withArchive(ARCHIVE)
				.withConfidentiality(ConfidentialityEmbeddable.create()
					.withConfidential(CONFIDENTIAL)
					.withLegalCitation(LEGAL_CITATION))
				.withCreatedBy(CREATED_BY)
				.withDescription(DESCRIPTION)
				.withDocumentData(List.of(
					DocumentDataEntity.create()
						.withFileName(FILE_1_NAME)
						.withFileSizeInBytes(FILE_1_SIZE_IN_BYTES)
						.withMimeType(MIME_TYPE_1),
					DocumentDataEntity.create()
						.withFileName(FILE_2_NAME)
						.withFileSizeInBytes(FILE_2_SIZE_IN_BYTES)
						.withMimeType(MIME_TYPE_2)))
				.withMetadata(List.of(DocumentMetadataEmbeddable.create()
					.withKey(METADATA_KEY)
					.withValue(METADATA_VALUE)))
				.withMunicipalityId(MUNICIPALITY_ID)
				.withRegistrationNumber(REGISTRATION_NUMBER)
				.withRevision(REVISION));
	}

	@Test
	void copyDocumentEntityWhenInputIsNull() {

		// Act
		final var result = DocumentMapper.copyDocumentEntity(null);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	void toPagedDocumentResponse(@Mock Page<DocumentEntity> pageMock) {

		// Arrange
		final var page = 1;
		final var pageSize = 20;
		final var sort = Sort.by(ASC, "property");
		final var pageable = PageRequest.of(page, pageSize, sort);

		final var documentEntity = DocumentEntity.create()
			.withConfidentiality(ConfidentialityEmbeddable.create()
				.withConfidential(CONFIDENTIAL)
				.withLegalCitation(LEGAL_CITATION))
			.withCreated(CREATED)
			.withCreatedBy(CREATED_BY)
			.withDescription(DESCRIPTION)
			.withId(ID)
			.withMetadata(List.of(DocumentMetadataEmbeddable.create()
				.withKey(METADATA_KEY)
				.withValue(METADATA_VALUE)))
			.withMunicipalityId(MUNICIPALITY_ID)
			.withRegistrationNumber(REGISTRATION_NUMBER)
			.withRevision(REVISION);

		when(pageMock.getPageable()).thenReturn(pageable);
		when(pageMock.getNumberOfElements()).thenReturn(11);
		when(pageMock.getTotalElements()).thenReturn(22L);
		when(pageMock.getTotalPages()).thenReturn(33);
		when(pageMock.getContent()).thenReturn(List.of(documentEntity));

		// Act
		final var result = DocumentMapper.toPagedDocumentResponse(pageMock);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getMetadata())
			.extracting(PagingMetaData::getPage, PagingMetaData::getLimit, PagingMetaData::getCount, PagingMetaData::getTotalRecords, PagingMetaData::getTotalPages)
			.containsExactly(page, pageSize, 11, 22L, 33);
		assertThat(result.getDocuments())
			.hasSize(1)
			.containsExactly(Document.create()
				.withConfidentiality(Confidentiality.create()
					.withConfidential(CONFIDENTIAL)
					.withLegalCitation(LEGAL_CITATION))
				.withCreated(CREATED)
				.withCreatedBy(CREATED_BY)
				.withDescription(DESCRIPTION)
				.withId(ID)
				.withMetadataList(List.of(DocumentMetadata.create()
					.withKey(METADATA_KEY)
					.withValue(METADATA_VALUE)))
				.withMunicipalityId(MUNICIPALITY_ID)
				.withRegistrationNumber(REGISTRATION_NUMBER)
				.withRevision(REVISION));
	}

	@Test
	void toPagedDocumentResponseWhenInputIsNull() {

		// Act
		final var result = DocumentMapper.toPagedDocumentResponse(null);

		// Assert
		assertThat(result).isNull();
	}

	@Test
	void toInclusionFilter() {
		// Act and assert
		assertThat(DocumentMapper.toInclusionFilter(false)).containsExactly(false);
		assertThat(DocumentMapper.toInclusionFilter(true)).containsExactlyInAnyOrder(true, false);
	}
}
