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
import java.util.Map;

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

		final var confidential = true;
		final var createdBy = "user";
		final var description = "description";
		final var metadataList = List.of(DocumentMetadata.create());
		final var municipalityId = "municipalityId";
		final var archiveMap = Map.of("filename", true, "filename2", false);

		final var bean = DocumentCreateRequest.create()
			.withConfidential(confidential)
			.withCreatedBy(createdBy)
			.withDescription(description)
			.withMetadataList(metadataList)
			.withMunicipalityId(municipalityId)
			.withArchiveMap(archiveMap);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.isConfidential()).isEqualTo(confidential);
		assertThat(bean.getCreatedBy()).isEqualTo(createdBy);
		assertThat(bean.getDescription()).isEqualTo(description);
		assertThat(bean.getMetadataList()).isEqualTo(metadataList);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getArchiveMap()).isEqualTo(archiveMap);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(DocumentCreateRequest.create()).hasAllNullFieldsOrPropertiesExcept("confidential");
		assertThat(new DocumentCreateRequest()).hasAllNullFieldsOrPropertiesExcept("confidential");
	}
}
