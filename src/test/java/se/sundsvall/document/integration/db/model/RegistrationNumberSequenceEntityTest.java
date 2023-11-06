package se.sundsvall.document.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.OffsetDateTime;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RegistrationNumberSequenceEntityTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		assertThat(RegistrationNumberSequenceEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var created = now(systemDefault());
		final var id = randomUUID().toString();
		final var modified = now(systemDefault());
		final var municipalityId = "municipalityId";
		final var sequenceNumber = 5;

		final var bean = RegistrationNumberSequenceEntity.create()
			.withCreated(created)
			.withId(id)
			.withModified(modified)
			.withMunicipalityId(municipalityId)
			.withSequenceNumber(sequenceNumber);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getModified()).isEqualTo(modified);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getSequenceNumber()).isEqualTo(sequenceNumber);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(RegistrationNumberSequenceEntity.create()).hasAllNullFieldsOrPropertiesExcept("sequenceNumber");
		assertThat(new RegistrationNumberSequenceEntity()).hasAllNullFieldsOrPropertiesExcept("sequenceNumber");
	}
}
