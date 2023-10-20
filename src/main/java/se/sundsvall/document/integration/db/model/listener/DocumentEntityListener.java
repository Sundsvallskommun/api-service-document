package se.sundsvall.document.integration.db.model.listener;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.MILLIS;

import jakarta.persistence.PrePersist;
import se.sundsvall.document.integration.db.model.DocumentEntity;

public class DocumentEntityListener {

	@PrePersist
	void prePersist(final DocumentEntity entity) {
		entity.setCreated(now(systemDefault()).truncatedTo(MILLIS));
	}
}
