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

import se.sundsvall.dept44.models.api.paging.PagingMetaData;

class PagedDocumentResponseTest {

	@Test
	void testBean() {
		assertThat(PagedDocumentResponse.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var documents = List.of(Document.create());
		final var pagingMetadata = PagingMetaData.create();

		final var bean = PagedDocumentResponse.create()
			.withDocuments(documents)
			.withMetaData(pagingMetadata);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getDocuments()).isEqualTo(documents);
		assertThat(bean.getMetadata()).isEqualTo(pagingMetadata);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(PagedDocumentResponse.create()).hasAllNullFieldsOrProperties();
		assertThat(new PagedDocumentResponse()).hasAllNullFieldsOrProperties();
	}
}
