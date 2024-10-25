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

class DocumentTypeUpdateRequestTest {

	@Test
	void testBean() {
		assertThat(DocumentTypeUpdateRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var updatedBy = "updatedBy";
		final var displayName = "displayName";

		final var bean = DocumentTypeUpdateRequest.create()
			.withDisplayName(displayName)
			.withUpdatedBy(updatedBy);

		assertThat(bean).hasNoNullFieldsOrProperties();
		assertThat(bean.getDisplayName()).isEqualTo(displayName);
		assertThat(bean.getUpdatedBy()).isEqualTo(updatedBy);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(DocumentTypeUpdateRequest.create()).hasAllNullFieldsOrProperties();
		assertThat(new DocumentTypeUpdateRequest()).hasAllNullFieldsOrProperties();
	}
}
