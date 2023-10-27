package se.sundsvall.document.integration.db.model.listener;

import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

import se.sundsvall.document.integration.db.model.RegistrationNumberSequenceEntity;

class RegistrationNumberSequenceEntityListenerTest {

	@Test
	void prePersist() {

		// Arrange
		final var listener = new RegistrationNumberSequenceEntityListener();
		final var entity = new RegistrationNumberSequenceEntity();

		// Act
		listener.prePersist(entity);

		// Assert
		assertThat(entity).hasAllNullFieldsOrPropertiesExcept("created", "sequenceNumber");
		assertThat(entity.getCreated()).isCloseTo(now(), within(2, SECONDS));
	}

	@Test
	void preUpdate() {

		// Arrange
		final var listener = new RegistrationNumberSequenceEntityListener();
		final var entity = new RegistrationNumberSequenceEntity();

		// Act
		listener.preUpdate(entity);

		// Assert
		assertThat(entity).hasAllNullFieldsOrPropertiesExcept("modified", "sequenceNumber");
		assertThat(entity.getModified()).isCloseTo(now(), within(2, SECONDS));
	}
}
