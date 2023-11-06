package se.sundsvall.document.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.mariadb.jdbc.MariaDbBlob;

class DocumentDataBinaryEntityTest {

	@Test
	void testBean() {
		assertThat(DocumentDataBinaryEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var binaryFile = new MariaDbBlob();
		final var id = randomUUID().toString();

		final var bean = DocumentDataBinaryEntity.create()
			.withBinaryFile(binaryFile)
			.withId(id);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getBinaryFile()).isEqualTo(binaryFile);
		assertThat(bean.getId()).isEqualTo(id);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(DocumentDataBinaryEntity.create()).hasAllNullFieldsOrProperties();
		assertThat(new DocumentDataBinaryEntity()).hasAllNullFieldsOrProperties();
	}
}
