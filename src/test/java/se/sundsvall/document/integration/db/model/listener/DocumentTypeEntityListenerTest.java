package se.sundsvall.document.integration.db.model.listener;

import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;
import se.sundsvall.document.integration.db.model.DocumentTypeEntity;

class DocumentTypeEntityListenerTest {

	@Test
	void prePersist() {

		// Arrange
		final var listener = new DocumentTypeEntityListener();
		final var entity = new DocumentTypeEntity();

		// Act
		listener.prePersist(entity);

		// Assert
		assertThat(entity).hasAllNullFieldsOrPropertiesExcept("created");
		assertThat(entity.getCreated()).isCloseTo(now(), within(2, SECONDS));
	}

	@Test
	void preUpdate() {

		// Arrange
		final var listener = new DocumentTypeEntityListener();
		final var entity = new DocumentTypeEntity();

		// Act
		listener.preUpdate(entity);

		// Assert
		assertThat(entity).hasAllNullFieldsOrPropertiesExcept("lastUpdated");
		assertThat(entity.getLastUpdated()).isCloseTo(now(), within(2, SECONDS));
	}
}
