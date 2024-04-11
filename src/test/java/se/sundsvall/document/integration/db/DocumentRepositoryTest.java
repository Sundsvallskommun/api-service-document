package se.sundsvall.document.integration.db;

import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static se.sundsvall.document.service.InclusionFilter.PUBLIC;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mariadb.jdbc.MariaDbBlob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import se.sundsvall.document.integration.db.model.ConfidentialityEmbeddable;
import se.sundsvall.document.integration.db.model.DocumentDataBinaryEntity;
import se.sundsvall.document.integration.db.model.DocumentDataEntity;
import se.sundsvall.document.integration.db.model.DocumentEntity;
import se.sundsvall.document.integration.db.model.DocumentMetadataEmbeddable;
import se.sundsvall.document.service.InclusionFilter;

/**
 * DocumentRepository tests.
 *
 * @see /src/test/resources/db/testdata-junit.sql for data setup.
 */
@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("junit")
@Sql(scripts = {
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-junit.sql"
})
class DocumentRepositoryTest {

	private static final String CREATED_BY = "User123";

	@Autowired
	private DocumentRepository documentRepository;

	@Test
	void create() {

		// Arrange
		final var registrationNumber = "2023-1337";
		final var entity = createDocumentEntity(registrationNumber);

		// Act
		final var result = documentRepository.save(entity);

		// Assert
		assertThat(result).isNotNull();
		assertThat(isValidUUID(result.getId())).isTrue();
		assertThat(result.getCreated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(result.getRegistrationNumber()).isEqualTo(registrationNumber);
		assertThat(result.getCreatedBy()).isEqualTo(CREATED_BY);
		assertThat(result.getMetadata())
			.extracting(DocumentMetadataEmbeddable::getKey, DocumentMetadataEmbeddable::getValue)
			.containsExactly(
				tuple("key1", "value1"),
				tuple("key2", "value2"));
	}

	@Test
	void createWithMultipleDocumentData() {

		// Arrange
		final var registrationNumber = "2023-1338";
		final var entity = createDocumentEntity(registrationNumber);

		entity.setDocumentData(List.of(createDocumentDataEntity("file1.txt"), createDocumentDataEntity("file2.txt")));

		// Act
		final var result = documentRepository.save(entity);

		// Assert
		assertThat(result).isNotNull();
		assertThat(isValidUUID(result.getId())).isTrue();
		assertThat(result.getCreated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(result.getRegistrationNumber()).isEqualTo(registrationNumber);
		assertThat(result.getCreatedBy()).isEqualTo(CREATED_BY);
		assertThat(result.getMetadata())
			.extracting(DocumentMetadataEmbeddable::getKey, DocumentMetadataEmbeddable::getValue)
			.containsExactly(
				tuple("key1", "value1"),
				tuple("key2", "value2"));
		assertThat(result.getDocumentData()).extracting(DocumentDataEntity::getFileName)
			.containsExactly("file1.txt", "file2.txt");
	}

	@Test
	void update() {

		// Arrange
		final var entity = documentRepository.findById("159c10bf-1b32-471b-b2d3-c4b4b13ea152").orElseThrow();
		assertThat(entity).isNotNull();
		assertThat(entity.getCreated()).isEqualTo(OffsetDateTime.parse("2023-06-28T12:01:00.000+02:00"));
		assertThat(entity.getRegistrationNumber()).isEqualTo("2023-2281-123");
		assertThat(entity.getCreatedBy()).isEqualTo("User1");
		assertThat(entity.getMetadata())
			.extracting(DocumentMetadataEmbeddable::getKey, DocumentMetadataEmbeddable::getValue)
			.containsExactly(tuple("document1-key1", "value-1"));

		// Act
		entity.withMetadata(new ArrayList<>(List.of(DocumentMetadataEmbeddable.create().withKey("UpdatedKey").withValue("UpdatedValue"))));
		final var result = documentRepository.save(entity);

		// Assert
		assertThat(result).isNotNull();
		assertThat(isValidUUID(result.getId())).isTrue();
		assertThat(entity.getCreated()).isEqualTo(OffsetDateTime.parse("2023-06-28T12:01:00.000+02:00"));
		assertThat(result.getRegistrationNumber()).isEqualTo("2023-2281-123");
		assertThat(result.getCreatedBy()).isEqualTo("User1");
		assertThat(result.getMetadata())
			.extracting(DocumentMetadataEmbeddable::getKey, DocumentMetadataEmbeddable::getValue)
			.containsExactly(tuple("UpdatedKey", "UpdatedValue"));
	}

	@ParameterizedTest
	@MethodSource("publicConfidentialTestsArgumentsProvider")
	void findTopByRegistrationNumberOrderByRevisionDesc(String registrationNumber, InclusionFilter filter, boolean shouldHaveMatch) {

		// Act
		final var result = documentRepository.findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(registrationNumber, filter.getValue());

		// Assert
		if (shouldHaveMatch) {
			assertThat(result).isPresent();
		} else {
			assertThat(result).isNotPresent();
		}
	}

	@Test
	void findTopByRegistrationNumberOrderByRevisionDesc() {

		// Arrange
		final var registrationNumber = "2023-2281-123"; // Document 1 (public)

		// Act
		final var result = documentRepository.findTopByRegistrationNumberAndConfidentialityConfidentialInOrderByRevisionDesc(registrationNumber, PUBLIC.getValue()).orElseThrow();

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo("612dc8d0-e6b7-426c-abcc-c9b49ae1e7e2");
		assertThat(result.getRevision()).isEqualTo(3);
		assertThat(result.getCreated()).isEqualTo(OffsetDateTime.parse("2023-06-28T12:03:00.000+02:00"));
		assertThat(result.getRegistrationNumber()).isEqualTo(registrationNumber);
		assertThat(result.getCreatedBy()).isEqualTo("User3");
		assertThat(result.getMetadata())
			.extracting(DocumentMetadataEmbeddable::getKey, DocumentMetadataEmbeddable::getValue)
			.containsExactly(
				tuple("document1-key1", "value-1"),
				tuple("document1-key2", "value-2"),
				tuple("document1-key3", "value-3"),
				tuple("document1-key4", "value-4"));
	}

	@Test
	void findByRegistrationNumberAndConfidentialInReturningList() {

		// Arrange
		final var registrationNumber = "2023-2281-123"; // Document 1 (public)

		// Act
		final var result = documentRepository.findByRegistrationNumberAndConfidentialityConfidentialIn(registrationNumber, PUBLIC.getValue());

		// Assert
		assertThat(result)
			.isNotNull()
			.extracting(DocumentEntity::getRegistrationNumber, DocumentEntity::getId, DocumentEntity::getRevision, DocumentEntity::getCreatedBy)
			.containsExactlyInAnyOrder(
				tuple("2023-2281-123", "159c10bf-1b32-471b-b2d3-c4b4b13ea152", 1, "User1"),
				tuple("2023-2281-123", "8efd63a3-b525-4581-8b0b-9759f381a5a5", 2, "User2"),
				tuple("2023-2281-123", "612dc8d0-e6b7-426c-abcc-c9b49ae1e7e2", 3, "User3"));
	}

	@ParameterizedTest
	@MethodSource("publicConfidentialTestsArgumentsProvider")
	void findByRegistrationNumberAndConfidentialInReturningPage(String registrationNumber, InclusionFilter filter, boolean shouldHaveMatch) {

		// Arrange
		final var pageRequest = PageRequest.of(0, 10, Sort.by(DESC, "revision"));

		// Act
		final var result = documentRepository.findByRegistrationNumberAndConfidentialityConfidentialIn(registrationNumber, filter.getValue(), pageRequest);

		// Assert
		if (shouldHaveMatch) {
			assertThat(result).isNotEmpty();
		} else {
			assertThat(result).isNullOrEmpty();
		}
	}

	@Test
	void findByRegistrationNumberAndConfidentialInReturningPage() {

		// Arrange
		final var registrationNumber = "2023-2281-123";
		final var pageRequest = PageRequest.of(0, 10, Sort.by(DESC, "revision"));

		// Act
		final var result = documentRepository.findByRegistrationNumberAndConfidentialityConfidentialIn(registrationNumber, PUBLIC.getValue(), pageRequest);

		// Assert
		assertThat(result)
			.hasSize(3)
			.extracting(DocumentEntity::getId, DocumentEntity::getRevision, DocumentEntity::getRegistrationNumber, DocumentEntity::getCreatedBy)
			.containsExactly(
				tuple("612dc8d0-e6b7-426c-abcc-c9b49ae1e7e2", 3, "2023-2281-123", "User3"),
				tuple("8efd63a3-b525-4581-8b0b-9759f381a5a5", 2, "2023-2281-123", "User2"),
				tuple("159c10bf-1b32-471b-b2d3-c4b4b13ea152", 1, "2023-2281-123", "User1"));
	}

	@ParameterizedTest
	@MethodSource("publicConfidentialTestsArgumentsProvider")
	void findByRegistrationNumberAndConfidentialInReturningPageReversedOrder(String registrationNumber, InclusionFilter filter, boolean shouldHaveMatch) {
		// Arrange
		final var pageRequest = PageRequest.of(0, 10, Sort.by(ASC, "revision"));

		// Act
		final var result = documentRepository.findByRegistrationNumberAndConfidentialityConfidentialIn(registrationNumber, filter.getValue(), pageRequest);

		// Assert
		if (shouldHaveMatch) {
			assertThat(result).isNotEmpty();
		} else {
			assertThat(result).isNullOrEmpty();
		}
	}

	@Test
	void findByRegistrationNumberAndConfidentialInReturningPageReversedOrder() {

		// Arrange
		final var registrationNumber = "2023-2281-123";
		final var pageRequest = PageRequest.of(0, 10, Sort.by(ASC, "revision"));

		// Act
		final var result = documentRepository.findByRegistrationNumberAndConfidentialityConfidentialIn(registrationNumber, PUBLIC.getValue(), pageRequest);

		// Assert
		assertThat(result)
			.hasSize(3)
			.extracting(DocumentEntity::getId, DocumentEntity::getRevision, DocumentEntity::getRegistrationNumber, DocumentEntity::getCreatedBy)
			.containsExactly(
				tuple("159c10bf-1b32-471b-b2d3-c4b4b13ea152", 1, "2023-2281-123", "User1"),
				tuple("8efd63a3-b525-4581-8b0b-9759f381a5a5", 2, "2023-2281-123", "User2"),
				tuple("612dc8d0-e6b7-426c-abcc-c9b49ae1e7e2", 3, "2023-2281-123", "User3"));
	}

	@ParameterizedTest
	@MethodSource("publicConfidentialTestsArgumentsProvider")
	void findByRegistrationNumberAndRevisionAndConfidentialIn(String registrationNumber, InclusionFilter filter, boolean shouldHaveMatch) {
		final var revision = 1;

		// Act
		final var result = documentRepository.findByRegistrationNumberAndRevisionAndConfidentialityConfidentialIn(registrationNumber, revision, filter.getValue());

		// Assert
		if (shouldHaveMatch) {
			assertThat(result).isPresent();
		} else {
			assertThat(result).isNotPresent();
		}
	}

	@Test
	void findByRegistrationNumberAndRevisionAndConfidentialIn() {

		// Arrange
		final var registrationNumber = "2023-2281-123";
		final var revision = 2;

		// Act
		final var result = documentRepository.findByRegistrationNumberAndRevisionAndConfidentialityConfidentialIn(registrationNumber, revision, PUBLIC.getValue()).orElseThrow();

		// Assert
		assertThat(result)
			.isNotNull()
			.extracting(DocumentEntity::getId, DocumentEntity::getRevision, DocumentEntity::getRegistrationNumber, DocumentEntity::getCreatedBy)
			.containsExactly("8efd63a3-b525-4581-8b0b-9759f381a5a5", 2, "2023-2281-123", "User2");
	}

	@Test
	void searchByKeyWithConfidentialIncluded() {

		// Arrange
		final var search = "*key3";
		final var pageRequest = PageRequest.of(0, 10, Sort.by(ASC, "created"));

		// Act
		final var result = documentRepository.search(search, true, pageRequest);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getContent())
			.extracting(DocumentEntity::getId, DocumentEntity::getRevision, DocumentEntity::getRegistrationNumber, DocumentEntity::getCreatedBy)
			.containsExactly(
				tuple("03d33a6a-bc8c-410c-95f6-2c890822967d", 1, "2024-2281-999", "User4"),
				tuple("612dc8d0-e6b7-426c-abcc-c9b49ae1e7e2", 3, "2023-2281-123", "User3"));
	}

	@Test
	void searchByKeyWithConfidentialExcluded() {

		// Arrange
		final var search = "*key3";
		final var pageRequest = PageRequest.of(0, 10, Sort.by(ASC, "created"));

		// Act
		final var result = documentRepository.search(search, false, pageRequest);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getContent())
			.extracting(DocumentEntity::getId, DocumentEntity::getRevision, DocumentEntity::getRegistrationNumber, DocumentEntity::getCreatedBy)
			.containsExactly(
				tuple("612dc8d0-e6b7-426c-abcc-c9b49ae1e7e2", 3, "2023-2281-123", "User3"));
	}

	@Test
	void searchByFilename() {

		// Arrange
		final var search = "file*";
		final var pageRequest = PageRequest.of(0, 10, Sort.by(ASC, "registrationNumber", "revision"));

		// Act
		final var result = documentRepository.search(search, true, pageRequest);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getContent())
			.extracting(DocumentEntity::getId, DocumentEntity::getRevision, DocumentEntity::getRegistrationNumber, DocumentEntity::getCreatedBy)
			.containsExactly(
				tuple("159c10bf-1b32-471b-b2d3-c4b4b13ea152", 1, "2023-2281-123", "User1"),
				tuple("8efd63a3-b525-4581-8b0b-9759f381a5a5", 2, "2023-2281-123", "User2"),
				tuple("612dc8d0-e6b7-426c-abcc-c9b49ae1e7e2", 3, "2023-2281-123", "User3"),
				tuple("03d33a6a-bc8c-410c-95f6-2c890822967d", 1, "2024-2281-999", "User4"));
	}

	@Test
	void searchNoMatches() {

		// Arrange
		final var search = "this-string-does-not-exists";
		final var pageRequest = PageRequest.of(0, 10, Sort.by(ASC, "created"));

		// Act
		final var result = documentRepository.search(search, true, pageRequest);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getContent()).isEmpty();
	}

	private static DocumentEntity createDocumentEntity(String registrationNumber) {
		return DocumentEntity.create()
			.withCreatedBy(CREATED_BY)
			.withConfidentiality(ConfidentialityEmbeddable.create())
			.withDescription("description")
			.withMetadata(List.of(
				DocumentMetadataEmbeddable.create().withKey("key1").withValue("value1"),
				DocumentMetadataEmbeddable.create().withKey("key2").withValue("value2")))
			.withRegistrationNumber(registrationNumber)
			.withRevision(0);
	}

	private static DocumentDataEntity createDocumentDataEntity(String filename) {
		final var fileContent = "fileContent";
		return DocumentDataEntity.create()
			.withDocumentDataBinary(DocumentDataBinaryEntity.create().withBinaryFile(new MariaDbBlob(fileContent.getBytes())))
			.withFileName(filename)
			.withFileSizeInBytes(fileContent.length())
			.withMimeType("text/plain");
	}

	private boolean isValidUUID(final String value) {
		try {
			UUID.fromString(String.valueOf(value));
		} catch (final Exception e) {
			return false;
		}

		return true;
	}

	private static Stream<Arguments> publicConfidentialTestsArgumentsProvider() {
		final var publicDocument = "2023-2281-123"; // Document 1 (public)
		final var confidentialDocument = "2024-2281-999"; // Document 2 (confidential)

		return Stream.of(
			Arguments.of(publicDocument, InclusionFilter.CONFIDENTIAL_AND_PUBLIC, true),
			Arguments.of(publicDocument, InclusionFilter.PUBLIC, true),
			Arguments.of(confidentialDocument, InclusionFilter.CONFIDENTIAL_AND_PUBLIC, true),
			Arguments.of(confidentialDocument, InclusionFilter.PUBLIC, false));
	}
}
