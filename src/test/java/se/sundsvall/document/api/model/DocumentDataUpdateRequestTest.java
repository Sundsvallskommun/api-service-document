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

class DocumentDataUpdateRequestTest {

	@Test
	void testBean() {
		assertThat(DocumentDataUpdateRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var createdBy = "user";

		final var bean = DocumentDataUpdateRequest.create()
			.withCreatedBy(createdBy);
		
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getCreatedBy()).isEqualTo(createdBy);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(DocumentDataUpdateRequest.create()).hasAllNullFieldsOrPropertiesExcept("archive");
		assertThat(new DocumentDataUpdateRequest()).hasAllNullFieldsOrPropertiesExcept("archive");
	}
}
