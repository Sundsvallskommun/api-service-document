package se.sundsvall.document.integration.db.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DocumentTypeIdTest {

	private static final String MUNICIPALITY_ID = "municipalityId";
	private static final String TYPE = "type";
	private static final DocumentTypeId BEAN = new DocumentTypeId(MUNICIPALITY_ID, TYPE);

	@Test
	void testConstructor() {
		assertThat(BEAN)
			.isNotNull()
			.hasNoNullFieldsOrProperties()
			.hasFieldOrPropertyWithValue("municipalityId", MUNICIPALITY_ID)
			.hasFieldOrPropertyWithValue("type", TYPE);
	}

	@Test
	void testToString() {
		assertThat(BEAN).hasToString("DocumentTypeId [municipalityId=municipalityId, type=type]");
	}

	@Test
	void testEquals() {
		final var beanWithSameContent = new DocumentTypeId(MUNICIPALITY_ID, TYPE);
		final var beanWithOtherContent1 = new DocumentTypeId("other" + MUNICIPALITY_ID, TYPE);
		final var beanWithOtherContent2 = new DocumentTypeId(MUNICIPALITY_ID, "other" + TYPE);
		new DocumentTypeId("other" + MUNICIPALITY_ID, TYPE);

		final var beanOfOtherClass = new Object();

		assertThat(BEAN)
			.isEqualTo(BEAN)
			.isEqualTo(beanWithSameContent)
			.isNotEqualTo(beanWithOtherContent1)
			.isNotEqualTo(beanWithOtherContent2)
			.isNotEqualTo(beanOfOtherClass)
			.isNotEqualTo(null);
	}

	@Test
	void testHashCode() {
		final var beanWithSameContent = new DocumentTypeId(MUNICIPALITY_ID, TYPE);
		final var beanWithOtherContent = new DocumentTypeId("other" + MUNICIPALITY_ID, "other" + TYPE);
		final var beanOfOtherClass = new Object();

		assertThat(BEAN)
			.hasSameHashCodeAs(BEAN)
			.hasSameHashCodeAs(beanWithSameContent)
			.doesNotHaveSameHashCodeAs(beanWithOtherContent)
			.doesNotHaveSameHashCodeAs(beanOfOtherClass);
	}

}
