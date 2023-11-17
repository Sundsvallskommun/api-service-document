package se.sundsvall.document.api;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import jakarta.servlet.http.HttpServletResponse;
import se.sundsvall.document.Application;
import se.sundsvall.document.api.model.Document;
import se.sundsvall.document.api.model.PagedDocumentResponse;
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
		final var pageRequest = PageRequest.of(0, 20);

		when(documentServiceMock.readAll(any(), anyBoolean(), any()))
			.thenReturn(PagedDocumentResponse.create().withDocuments(List.of(Document.create())));

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/documents/" + registrationNumber + "/revisions")
				.queryParam("page", pageRequest.getPageNumber())
				.queryParam("size", pageRequest.getPageSize())
				.build())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(PagedDocumentResponse.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getDocuments()).hasSize(1);
		verify(documentServiceMock).readAll(registrationNumber, false, pageRequest);
	}

	@Test
	void readAllWithIncludeConfidential() {

		// Arrange
		final var includeConfidential = true;
		final var registrationNumber = "2023-2281-1337";
		final var pageRequest = PageRequest.of(0, 20);

		when(documentServiceMock.readAll(any(), anyBoolean(), any()))
			.thenReturn(PagedDocumentResponse.create().withDocuments(List.of(Document.create())));

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/documents/" + registrationNumber + "/revisions")
				.queryParam("page", pageRequest.getPageNumber())
				.queryParam("size", pageRequest.getPageSize())
				.queryParam("includeConfidential", includeConfidential)
				.build())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(PagedDocumentResponse.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getDocuments()).hasSize(1);
		verify(documentServiceMock).readAll(registrationNumber, includeConfidential, pageRequest);
	}

	@Test
	void read() {

		// Arrange
		final var registrationNumber = "2023-2281-1337";
		final var revision = 2;

		when(documentServiceMock.read(any(), anyInt(), anyBoolean())).thenReturn(Document.create());

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
		verify(documentServiceMock).read(registrationNumber, revision, false);
	}

	@Test
	void readWithIncludeConfidential() {

		// Arrange
		final var includeConfidential = true;
		final var registrationNumber = "2023-2281-1337";
		final var revision = 2;

		when(documentServiceMock.read(any(), anyInt(), anyBoolean())).thenReturn(Document.create());

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/documents/" + registrationNumber + "/revisions/" + revision)
				.queryParam("includeConfidential", includeConfidential)
				.build())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(Document.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		verify(documentServiceMock).read(registrationNumber, revision, includeConfidential);
	}

	@Test
	void readFile() {

		// Arrange
		final var registrationNumber = "2023-2281-1337";
		final var documentDataId = randomUUID().toString();
		final var revision = 2;

		// Act
		webTestClient.get()
			.uri("/documents/" + registrationNumber + "/revisions/" + revision + "/files/" + documentDataId)
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.isEmpty();

		// Assert
		verify(documentServiceMock).readFile(eq(registrationNumber), eq(revision), eq(documentDataId), eq(false), any(HttpServletResponse.class));
	}

	@Test
	void readFileWithIncludeConfidential() {

		// Arrange
		final var includeConfidential = true;
		final var registrationNumber = "2023-2281-1337";
		final var documentDataId = randomUUID().toString();
		final var revision = 2;

		// Act
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/documents/" + registrationNumber + "/revisions/" + revision + "/files/" + documentDataId)
				.queryParam("includeConfidential", includeConfidential)
				.build())
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.isEmpty();

		// Assert
		verify(documentServiceMock).readFile(eq(registrationNumber), eq(revision), eq(documentDataId), eq(includeConfidential), any(HttpServletResponse.class));
	}
}
