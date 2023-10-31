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

class DocumentDataEntityTest {

	@Test
	void testBean() {
		assertThat(DocumentDataEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var file = new MariaDbBlob();
		final var fileName = "filename.jpg";
		final var id = randomUUID().toString();
		final var mimeType = "image/jpeg";
		final var fileSizeInBytes = 100;

		final var bean = DocumentDataEntity.create()
			.withFile(file)
			.withFileName(fileName)
			.withId(id)
			.withMimeType(mimeType)
			.withFileSizeInBytes(fileSizeInBytes);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getFile()).isEqualTo(file);
		assertThat(bean.getFileName()).isEqualTo(fileName);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getMimeType()).isEqualTo(mimeType);
		assertThat(bean.getFileSizeInBytes()).isEqualTo(fileSizeInBytes);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(DocumentDataEntity.create()).hasAllNullFieldsOrPropertiesExcept("fileSizeInBytes");
		assertThat(new DocumentDataEntity()).hasAllNullFieldsOrPropertiesExcept("fileSizeInBytes");
	}
}
