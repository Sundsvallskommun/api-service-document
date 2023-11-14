package se.sundsvall.document.service;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.document.service.InclusionFilter.CONFIDENTIAL_AND_PUBLIC;
import static se.sundsvall.document.service.InclusionFilter.PUBLIC;

import org.junit.jupiter.api.Test;

class InclusionFilterTest {

	@Test
	void enums() {
		assertThat(InclusionFilter.values()).containsExactlyInAnyOrder(CONFIDENTIAL_AND_PUBLIC, PUBLIC);
	}

	@Test
	void enumValues() {
		assertThat(CONFIDENTIAL_AND_PUBLIC.getValue()).containsExactlyInAnyOrder(true, false);
		assertThat(PUBLIC.getValue()).containsExactly(false);
	}
}
