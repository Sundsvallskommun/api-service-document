package se.sundsvall.document.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

/**
 * RegistrationNumberSequenceRepositoryTest tests.
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
class RegistrationNumberSequenceRepositoryTest {

	private static final String MUNICIPALITY_ID = "2321";

	@Autowired
	private RegistrationNumberSequenceRepository registrationNumberSequenceRepository;

	@Test
	void findByMunicipalityId() {

		// Act
		final var result = registrationNumberSequenceRepository.findByMunicipalityId(MUNICIPALITY_ID).orElseThrow();

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getCreated()).isEqualTo(OffsetDateTime.parse("2023-06-28T12:01:00.000+02:00"));
		assertThat(isValidUUID(result.getId())).isTrue();
		assertThat(result.getModified()).isEqualTo(OffsetDateTime.parse("2023-06-28T12:01:00.000+02:00"));
		assertThat(result.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(result.getSequenceNumber()).isEqualTo(665);
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
