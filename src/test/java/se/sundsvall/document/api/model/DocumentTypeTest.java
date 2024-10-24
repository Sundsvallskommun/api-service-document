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

class DocumentTypeTest {

	@Test
	void testBean() {
		assertThat(DocumentType.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var displayName = "displayName";
		final var type = "type";

		final var bean = DocumentType.create()
			.withDisplayName(displayName)
			.withType(type);

		assertThat(bean).hasNoNullFieldsOrProperties();
		assertThat(bean.getDisplayName()).isEqualTo(displayName);
		assertThat(bean.getType()).isEqualTo(type);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(DocumentType.create()).hasAllNullFieldsOrProperties();
		assertThat(new DocumentType()).hasAllNullFieldsOrProperties();
	}
}
