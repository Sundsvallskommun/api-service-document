package se.sundsvall.document.integration.db.model.listener;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.MILLIS;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import se.sundsvall.document.integration.db.model.DocumentTypeEntity;

public class DocumentTypeEntityListener {

	@PrePersist
	void prePersist(final DocumentTypeEntity entity) {
		entity.setCreated(now(systemDefault()).truncatedTo(MILLIS));
	}

	@PreUpdate
	void preUpdate(final DocumentTypeEntity entity) {
		entity.setLastUpdated(now(systemDefault()).truncatedTo(MILLIS));
	}
}
