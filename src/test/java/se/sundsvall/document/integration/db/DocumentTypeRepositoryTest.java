package se.sundsvall.document.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

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
class DocumentTypeRepositoryTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String TYPE = "EMPLOYEE_CERTIFICATE";

	@Autowired
	private DocumentTypeRepository repository;

	@Test
	void findByMunicipalityId() {
		final var matches = repository.findAllByMunicipalityId(MUNICIPALITY_ID);

		assertThat(matches).hasSize(2).satisfiesExactlyInAnyOrder(match -> {
			assertThat(match.getId()).isEqualTo("86b9efc9-c649-40d5-ade0-ac415ea146f1");
			assertThat(match.getMunicipalityId()).isEqualTo("2281");
			assertThat(match.getType()).isEqualTo("EMPLOYEE_CERTIFICATE");
			assertThat(match.getDisplayName()).isEqualTo("Anställningsbevis");
			assertThat(match.getCreatedBy()).isEqualTo("User1");
		}, match -> {
			assertThat(match.getId()).isEqualTo("3fdecd8b-d295-4222-b60c-e95ba5f5075a");
			assertThat(match.getMunicipalityId()).isEqualTo("2281");
			assertThat(match.getType()).isEqualTo("HOLIDAY_EXCHANGE");
			assertThat(match.getDisplayName()).isEqualTo("Semesterväxlingsdokument");
			assertThat(match.getCreatedBy()).isEqualTo("User2");
		});
	}

	@Test
	void findByMunicipalityIdAndType() {
		final var result = repository.findByMunicipalityIdAndType(MUNICIPALITY_ID, TYPE);

		assertThat(result).isPresent().hasValueSatisfying(match -> {
			assertThat(match.getId()).isEqualTo("86b9efc9-c649-40d5-ade0-ac415ea146f1");
			assertThat(match.getMunicipalityId()).isEqualTo("2281");
			assertThat(match.getType()).isEqualTo("EMPLOYEE_CERTIFICATE");
			assertThat(match.getDisplayName()).isEqualTo("Anställningsbevis");
			assertThat(match.getCreatedBy()).isEqualTo("User1");
		});
	}

}
