package se.sundsvall.document.integration.db.model.listener;

import jakarta.persistence.PrePersist;
import se.sundsvall.document.integration.db.model.DocumentEntity;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.MILLIS;

public class DocumentEntityListener {

	@PrePersist
	void prePersist(final DocumentEntity entity) {
		entity.setCreated(now(systemDefault()).truncatedTo(MILLIS));
	}
}
