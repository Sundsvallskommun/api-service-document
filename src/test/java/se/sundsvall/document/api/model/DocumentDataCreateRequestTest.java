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

class DocumentDataCreateRequestTest {

	@Test
	void testBean() {
		assertThat(DocumentDataCreateRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var createdBy = "user";

		final var bean = DocumentDataCreateRequest.create()
			.withCreatedBy(createdBy);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getCreatedBy()).isEqualTo(createdBy);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(DocumentDataCreateRequest.create()).hasAllNullFieldsOrProperties();
		assertThat(new DocumentDataCreateRequest()).hasAllNullFieldsOrProperties();
	}
}
