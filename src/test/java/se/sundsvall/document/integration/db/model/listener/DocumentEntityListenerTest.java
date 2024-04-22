package se.sundsvall.document.integration.db.model.listener;

import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

import se.sundsvall.document.integration.db.model.DocumentEntity;

class DocumentEntityListenerTest {

	@Test
	void prePersist() {

		// Arrange
		final var listener = new DocumentEntityListener();
		final var entity = new DocumentEntity();

		// Act
		listener.prePersist(entity);

		// Assert
		assertThat(entity).hasAllNullFieldsOrPropertiesExcept("archive", "created", "revision");
		assertThat(entity.getCreated()).isCloseTo(now(), within(2, SECONDS));
		assertThat(entity.getRevision()).isZero();
	}
}
