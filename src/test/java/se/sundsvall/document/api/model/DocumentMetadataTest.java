package se.sundsvall.document.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

class DocumentMetadataTest {

	@Test
	void testBean() {
		assertThat(DocumentMetadata.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var key = "key";
		final var value = "value";

		final var bean = DocumentMetadata.create()
			.withKey(key)
			.withValue(value);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getKey()).isEqualTo(key);
		assertThat(bean.getValue()).isEqualTo(value);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(DocumentMetadata.create()).hasAllNullFieldsOrProperties();
		assertThat(new DocumentMetadata()).hasAllNullFieldsOrProperties();
	}
}
