package se.sundsvall.document.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.document.Application;
import se.sundsvall.document.api.model.DocumentHeader;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class DocumentRevisionResourceTest {

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void readAll() {

		// Act
		final var response = webTestClient.get()
			.uri("/documents/2023-1337/revisions")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(DocumentHeader.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotEmpty();

		// TODO: Add verification
		// verifyNoInteractions(serviceMock);
	}

	@Test
	void read() {

		// Act
		final var response = webTestClient.get()
			.uri("/documents/2023-1337/revisions/2")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(DocumentHeader.class)
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
			.uri("/documents/2023-1337/revisions/2/file")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNull(); // TODO: Change when result is mocked

		// TODO: Add verification
		// verifyNoInteractions(serviceMock);
	}
}
