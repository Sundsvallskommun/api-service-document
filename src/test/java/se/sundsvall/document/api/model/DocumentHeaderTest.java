package se.sundsvall.document.api.model;

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
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DocumentHeaderTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		assertThat(DocumentHeader.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var created = now(systemDefault());
		final var createdBy = "user";
		final var id = randomUUID().toString();
		final var metadataList = List.of(DocumentMetadata.create());
		final var registrationNumber = "12345";
		final var revision = 5;

		final var bean = DocumentHeader.create()
			.withCreated(created)
			.withCreatedBy(createdBy)
			.withId(id)
			.withMetadataList(metadataList)
			.withRegistrationNumber(registrationNumber)
			.withRevision(revision);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getCreated()).isEqualTo(created);
		assertThat(bean.getCreatedBy()).isEqualTo(createdBy);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getMetadataList()).isEqualTo(metadataList);
		assertThat(bean.getRegistrationNumber()).isEqualTo(registrationNumber);
		assertThat(bean.getRevision()).isEqualTo(revision);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(DocumentHeader.create()).hasAllNullFieldsOrPropertiesExcept("revision");
		assertThat(new DocumentHeader()).hasAllNullFieldsOrPropertiesExcept("revision");
	}
}
