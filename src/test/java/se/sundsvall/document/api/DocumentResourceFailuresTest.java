package se.sundsvall.document.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.web.reactive.function.BodyInserters.fromMultipartData;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;

import se.sundsvall.document.Application;
import se.sundsvall.document.api.model.Document;
import se.sundsvall.document.api.model.DocumentMetadata;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class DocumentResourceFailuresTest {

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void createWithMissingDocumentFile() {

		// Arrange
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("document", Document.create());

		// Act
		final var response = webTestClient.post()
			.uri("/documents")
			.contentType(MULTIPART_FORM_DATA)
			.body(fromMultipartData(multipartBodyBuilder.build()))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getDetail()).isEqualTo("Required part 'documentFile' is not present.");

		// TODO: Add verification
		// verifyNoInteractions(serviceMock);
	}

	@Test
	void createWithMissingDocument() {

		// Arrange
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("documentFile", "file-content").filename("test.txt").contentType(TEXT_PLAIN);

		// Act
		final var response = webTestClient.post()
			.uri("/documents")
			.contentType(MULTIPART_FORM_DATA)
			.body(fromMultipartData(multipartBodyBuilder.build()))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getDetail()).isEqualTo("Required part 'document' is not present.");

		// TODO: Add verification
		// verifyNoInteractions(serviceMock);
	}

	@Test
	void updateWithBlankKeyInMetaData() {

		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("document", Document.create()
			.withMetadataList(List.of(
				DocumentMetadata.create()
					.withKey(" ")
					.withValue("value"))));

		// Act
		final var response = webTestClient.patch()
			.uri("/documents/2023-1337")
			.contentType(MULTIPART_FORM_DATA)
			.body(fromMultipartData(multipartBodyBuilder.build()))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("metadataList[0].key", "must not be blank"));

		// TODO: Add verification
		// verifyNoInteractions(serviceMock);
	}

	@Test
	void updateWithBlankValueInMetaData() {

		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("document", Document.create()
			.withMetadataList(List.of(
				DocumentMetadata.create()
					.withKey("key")
					.withValue(" "))));

		// Act
		final var response = webTestClient.patch()
			.uri("/documents/2023-1337")
			.contentType(MULTIPART_FORM_DATA)
			.body(fromMultipartData(multipartBodyBuilder.build()))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("metadataList[0].value", "must not be blank"));

		// TODO: Add verification
		// verifyNoInteractions(serviceMock);
	}
}
