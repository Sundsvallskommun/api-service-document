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

class ConfidentialityTest {

	@Test
	void testBean() {
		assertThat(Confidentiality.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var confidential = true;
		final var legalCitation = "legalCitation";

		final var bean = Confidentiality.create()
			.withConfidential(confidential)
			.withLegalCitation(legalCitation);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.isConfidential()).isEqualTo(confidential);
		assertThat(bean.getLegalCitation()).isEqualTo(legalCitation);

	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Confidentiality.create()).hasAllNullFieldsOrPropertiesExcept("confidential");
		assertThat(new Confidentiality()).hasAllNullFieldsOrPropertiesExcept("confidential");
	}
}
