package se.sundsvall.document.api;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;

import se.sundsvall.document.Application;
import se.sundsvall.document.service.DocumentService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class DocumentRevisionResourceFailuresTest {

	@MockBean
	private DocumentService documentServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void readWithNegativeRevision() {

		// Act
		final var response = webTestClient.get()
			.uri("/documents/2023-1337/revisions/-1")
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("readRevision.revision", "must be greater than or equal to 0"));

		verifyNoInteractions(documentServiceMock);
	}

	@Test
	void readFileWithNegativeRevision() {

		// Arrange
		final var documentDataId = randomUUID().toString();

		// Act
		final var response = webTestClient.get()
			.uri("/documents/2023-1337/revisions/-1/files/" + documentDataId)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("readFileRevision.revision", "must be greater than or equal to 0"));

		verifyNoInteractions(documentServiceMock);
	}

	@Test
	void readFileWithInvalidDocumentDataId() {

		// Arrange
		final var documentDataId = "not-a-valid-uuid";

		// Act
		final var response = webTestClient.get()
			.uri("/documents/2023-1337/revisions/1/files/" + documentDataId)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("readFileRevision.documentDataId", "not a valid UUID"));

		verifyNoInteractions(documentServiceMock);
	}
}
