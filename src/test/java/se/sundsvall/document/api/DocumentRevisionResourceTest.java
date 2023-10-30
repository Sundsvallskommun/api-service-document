package se.sundsvall.document.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import jakarta.servlet.http.HttpServletResponse;
import se.sundsvall.document.Application;
import se.sundsvall.document.api.model.Document;
import se.sundsvall.document.service.DocumentService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class DocumentRevisionResourceTest {

	@MockBean
	private DocumentService documentServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void readAll() {

		// Arrange
		final var registrationNumber = "2023-2281-1337";

		when(documentServiceMock.readAll(registrationNumber)).thenReturn(List.of(Document.create()));

		// Act
		final var response = webTestClient.get()
			.uri("/documents/" + registrationNumber + "/revisions")
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(Document.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotEmpty();
		verify(documentServiceMock).readAll(registrationNumber);
	}

	@Test
	void read() {

		// Arrange
		final var registrationNumber = "2023-2281-1337";
		final var revision = 2;

		when(documentServiceMock.read(registrationNumber, revision)).thenReturn(Document.create());

		// Act
		final var response = webTestClient.get()
			.uri("/documents/" + registrationNumber + "/revisions/" + revision)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(Document.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		verify(documentServiceMock).read(registrationNumber, revision);
	}

	@Test
	void readFile() {

		// Arrange
		final var registrationNumber = "2023-2281-1337";
		final var revision = 2;

		when(documentServiceMock.read(registrationNumber, revision)).thenReturn(Document.create());

		// Act
		webTestClient.get()
			.uri("/documents/" + registrationNumber + "/revisions/" + revision + "/file")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.isEmpty();

		// Assert
		verify(documentServiceMock).readFile(eq(registrationNumber), eq(revision), any(HttpServletResponse.class));
	}
}
