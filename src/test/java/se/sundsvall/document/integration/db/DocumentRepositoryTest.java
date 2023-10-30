package se.sundsvall.document.integration.db;

import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import se.sundsvall.document.integration.db.model.DocumentEntity;
import se.sundsvall.document.integration.db.model.DocumentMetadataEmbeddable;

/**
 * DocumentRepository tests
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

	private static final String REGISTRATION_NUMBER = "2023-1337";
	private static final String CREATED_BY = "User123";
	private static final String DOCUMENT_ENTITY_ID = "159c10bf-1b32-471b-b2d3-c4b4b13ea152"; // -- Document 1, revision 1

	@Autowired
	private DocumentRepository documentRepository;

	@Test
	void create() {

		// Arrange
		final var entity = createDocumentEntity();

		// Act
		final var result = documentRepository.save(entity);

		// Assert
		assertThat(result).isNotNull();
		assertThat(isValidUUID(result.getId())).isTrue();
		assertThat(result.getCreated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(result.getRegistrationNumber()).isEqualTo(REGISTRATION_NUMBER);
		assertThat(result.getCreatedBy()).isEqualTo(CREATED_BY);
		assertThat(result.getMetadata())
			.extracting(DocumentMetadataEmbeddable::getKey, DocumentMetadataEmbeddable::getValue)
			.containsExactly(
				tuple("key1", "value1"),
				tuple("key2", "value2"));
	}

	@Test
	void update() {

		// Arrange
		final var entity = documentRepository.findById(DOCUMENT_ENTITY_ID).orElseThrow();
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

	@Test
	void findTopByRegistrationNumberOrderByRevisionDesc() {

		// Arrange
		final var registrationNumber = "2023-2281-123";

		// Act
		final var result = documentRepository.findTopByRegistrationNumberOrderByRevisionDesc(registrationNumber).orElseThrow();

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo("612dc8d0-e6b7-426c-abcc-c9b49ae1e7e2");
		assertThat(result.getRevision()).isEqualTo(3);
		assertThat(result.getCreated()).isEqualTo(OffsetDateTime.parse("2023-06-28T12:03:00.000+02:00"));
		assertThat(result.getRegistrationNumber()).isEqualTo(registrationNumber);
		assertThat(result.getCreatedBy()).isEqualTo("User1");
		assertThat(result.getMetadata())
			.extracting(DocumentMetadataEmbeddable::getKey, DocumentMetadataEmbeddable::getValue)
			.containsExactly(
				tuple("document1-key1", "value-1"),
				tuple("document1-key2", "value-2"),
				tuple("document1-key3", "value-3"),
				tuple("document1-key4", "value-4"));
	}

	@Test
	void findByRegistrationNumber() {

		// Arrange
		final var registrationNumber = "2023-2281-123";

		// Act
		final var result = documentRepository.findByRegistrationNumberOrderByRevisionAsc(registrationNumber);

		// Assert
		assertThat(result)
			.hasSize(3)
			.extracting(DocumentEntity::getId, DocumentEntity::getRevision, DocumentEntity::getRegistrationNumber, DocumentEntity::getCreatedBy)
			.containsExactly(
				tuple("159c10bf-1b32-471b-b2d3-c4b4b13ea152", 1, "2023-2281-123", "User1"),
				tuple("8efd63a3-b525-4581-8b0b-9759f381a5a5", 2, "2023-2281-123", "User1"),
				tuple("612dc8d0-e6b7-426c-abcc-c9b49ae1e7e2", 3, "2023-2281-123", "User1"));
	}

	@Test
	void findByRegistrationNumberInReversedOrder() {

		// Arrange
		final var registrationNumber = "2023-2281-123";

		// Act
		final var result = documentRepository.findByRegistrationNumberOrderByRevisionAsc(registrationNumber);

		// Assert
		assertThat(result)
			.hasSize(3)
			.extracting(DocumentEntity::getId, DocumentEntity::getRevision, DocumentEntity::getRegistrationNumber, DocumentEntity::getCreatedBy)
			.containsExactly(
				tuple("159c10bf-1b32-471b-b2d3-c4b4b13ea152", 1, "2023-2281-123", "User1"),
				tuple("8efd63a3-b525-4581-8b0b-9759f381a5a5", 2, "2023-2281-123", "User1"),
				tuple("612dc8d0-e6b7-426c-abcc-c9b49ae1e7e2", 3, "2023-2281-123", "User1"));
	}

	@Test
	void findByRegistrationNumberAndRevision() {

		// Arrange
		final var registrationNumber = "2023-2281-123";
		final var revision = 2;

		// Act
		final var result = documentRepository.findByRegistrationNumberAndRevision(registrationNumber, revision).orElseThrow();

		// Assert
		assertThat(result)
			.isNotNull()
			.extracting(DocumentEntity::getId, DocumentEntity::getRevision, DocumentEntity::getRegistrationNumber, DocumentEntity::getCreatedBy)
			.containsExactly("8efd63a3-b525-4581-8b0b-9759f381a5a5", 2, "2023-2281-123", "User1");
	}

	private static DocumentEntity createDocumentEntity() {
		return DocumentEntity.create()
			.withCreatedBy(CREATED_BY)
			.withMetadata(List.of(
				DocumentMetadataEmbeddable.create().withKey("key1").withValue("value1"),
				DocumentMetadataEmbeddable.create().withKey("key2").withValue("value2")))
			.withRegistrationNumber(REGISTRATION_NUMBER)
			.withRevision(0);
	}

	private boolean isValidUUID(final String value) {
		try {
			UUID.fromString(String.valueOf(value));
		} catch (final Exception e) {
			return false;
		}

		return true;
	}
}
