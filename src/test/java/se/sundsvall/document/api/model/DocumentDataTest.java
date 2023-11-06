package se.sundsvall.document.api.model;

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

class DocumentDataTest {

	@Test
	void testBean() {
		assertThat(DocumentData.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var fileName = "file.png";
		final var fileSizeInBytes = 123L;
		final var id = randomUUID().toString();
		final var mimeType = "image/png";

		final var bean = DocumentData.create()
			.withFileName(fileName)
			.withFileSizeInBytes(fileSizeInBytes)
			.withId(id)
			.withMimeType(mimeType);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getFileName()).isEqualTo(fileName);
		assertThat(bean.getFileSizeInBytes()).isEqualTo(fileSizeInBytes);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getMimeType()).isEqualTo(mimeType);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(DocumentData.create()).hasAllNullFieldsOrPropertiesExcept("fileSizeInBytes");
		assertThat(new DocumentData()).hasAllNullFieldsOrPropertiesExcept("fileSizeInBytes");
	}
}
