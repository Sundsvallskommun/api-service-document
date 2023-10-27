package se.sundsvall.document.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class DocumentUpdateRequestTest {

	@Test
	void testBean() {
		assertThat(DocumentUpdateRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var createdBy = "user";
		final var metadataList = List.of(DocumentMetadata.create());

		final var bean = DocumentUpdateRequest.create()
			.withCreatedBy(createdBy)
			.withMetadataList(metadataList);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getCreatedBy()).isEqualTo(createdBy);
		assertThat(bean.getMetadataList()).isEqualTo(metadataList);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(DocumentUpdateRequest.create()).hasAllNullFieldsOrProperties();
		assertThat(new DocumentUpdateRequest()).hasAllNullFieldsOrProperties();
	}
}