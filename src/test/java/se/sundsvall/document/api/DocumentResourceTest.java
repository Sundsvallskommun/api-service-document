package se.sundsvall.document.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.web.reactive.function.BodyInserters.fromMultipartData;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.document.Application;
import se.sundsvall.document.api.model.Document;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class DocumentResourceTest {

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void create() {

		// Arrange
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("documentFile", "file-content").filename("test.txt").contentType(TEXT_PLAIN);
		multipartBodyBuilder.part("document", Document.create());

		// Act
		final var response = webTestClient.post()
			.uri("/documents")
			.contentType(MULTIPART_FORM_DATA)
			.body(fromMultipartData(multipartBodyBuilder.build()))
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL_VALUE)
			.expectHeader().exists(HttpHeaders.LOCATION)
			.expectBody().isEmpty();

		// Assert
		assertThat(response).isNotNull();

		// TODO: Add verification
		// verifyNoInteractions(serviceMock);
	}

	@Test
	void update() {

		// Arrange
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("documentFile", "file-content").filename("test.txt").contentType(TEXT_PLAIN);
		multipartBodyBuilder.part("document", Document.create());

		// Act
		final var response = webTestClient.patch()
			.uri("/documents/2023-1337")
			.contentType(MULTIPART_FORM_DATA)
			.body(fromMultipartData(multipartBodyBuilder.build()))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();

		// TODO: Add verification
		// verifyNoInteractions(serviceMock);
	}

	@Test
	void read() {

		// Act
		final var response = webTestClient.get()
			.uri("/documents/2023-1337")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();

		// TODO: Add verification
		// verifyNoInteractions(serviceMock);
	}

	@Test
	void readFile() {

		// Act
		final var response = webTestClient.get()
			.uri("/documents/2023-1337/file")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNull(); // TODO: change when result is mocked

		// TODO: Add verification
		// verifyNoInteractions(serviceMock);
	}
}
