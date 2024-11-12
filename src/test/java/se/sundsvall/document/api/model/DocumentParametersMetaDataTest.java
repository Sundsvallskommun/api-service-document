package se.sundsvall.document.api.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

class DocumentParametersMetaDataTest {

	@Test
	void testBean() {
		assertThat(DocumentParameters.MetaData.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters()));
	}

	@Test
	void testBuilderMethods() {
		var key = "key";
		var matchesAll = List.of("value1", "value2");
		var matchesAny = List.of("value1", "value2");

		final var bean = DocumentParameters.MetaData.create()
			.withKey(key)
			.withMatchesAny(matchesAny)
			.withMatchesAll(matchesAll);

		Assertions.assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();

	}

	@Test
	void testNoDirtOnCreatedBean() {
		Assertions.assertThat(DocumentParameters.MetaData.create()).hasAllNullFieldsOrProperties();
		Assertions.assertThat(new DocumentParameters.MetaData()).hasAllNullFieldsOrProperties();
	}
}
