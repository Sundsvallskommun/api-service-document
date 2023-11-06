package se.sundsvall.document.integration.db.model.listener;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.MILLIS;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import se.sundsvall.document.integration.db.model.RegistrationNumberSequenceEntity;

public class RegistrationNumberSequenceEntityListener {

	@PrePersist
	void prePersist(final RegistrationNumberSequenceEntity entity) {
		entity.setCreated(now(systemDefault()).truncatedTo(MILLIS));
	}

	@PreUpdate
	void preUpdate(final RegistrationNumberSequenceEntity entity) {
		entity.setModified(now(systemDefault()).truncatedTo(MILLIS));
	}
}
