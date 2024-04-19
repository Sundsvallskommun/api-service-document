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

class DocumentCreateRequestTest {

	@Test
	void testBean() {
		assertThat(DocumentCreateRequest.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var archive = true;
		final var confidentiality = Confidentiality.create().withConfidential(true).withLegalCitation("legalCitation");
		final var createdBy = "user";
		final var description = "description";
		final var metadataList = List.of(DocumentMetadata.create());
		final var municipalityId = "municipalityId";

		final var bean = DocumentCreateRequest.create()
			.withArchive(archive)
			.withConfidentiality(confidentiality)
			.withCreatedBy(createdBy)
			.withDescription(description)
			.withMetadataList(metadataList)
			.withMunicipalityId(municipalityId);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.isArchive()).isEqualTo(archive);
		assertThat(bean.getConfidentiality()).isEqualTo(confidentiality);
		assertThat(bean.getCreatedBy()).isEqualTo(createdBy);
		assertThat(bean.getDescription()).isEqualTo(description);
		assertThat(bean.getMetadataList()).isEqualTo(metadataList);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(DocumentCreateRequest.create()).hasAllNullFieldsOrPropertiesExcept("archive")
			.hasFieldOrPropertyWithValue("archive", false);
		assertThat(new DocumentCreateRequest()).hasAllNullFieldsOrPropertiesExcept("archive")
			.hasFieldOrPropertyWithValue("archive", false);
	}
}
