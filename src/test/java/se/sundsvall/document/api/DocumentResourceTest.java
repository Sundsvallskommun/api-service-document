package se.sundsvall.document.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.web.reactive.function.BodyInserters.fromMultipartData;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import se.sundsvall.document.Application;
import se.sundsvall.document.api.model.Document;
import se.sundsvall.document.api.model.DocumentCreateRequest;
import se.sundsvall.document.api.model.DocumentMetadata;
import se.sundsvall.document.api.model.DocumentUpdateRequest;
import se.sundsvall.document.service.DocumentService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class DocumentResourceTest {

	@MockBean
	private DocumentService documentServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void create() {

		// Arrange
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("documentFile", "file-content").filename("test.txt").contentType(TEXT_PLAIN);
		multipartBodyBuilder.part("document", DocumentCreateRequest.create()
			.withCreatedBy("user")
			.withMunicipalityId("2281")
			.withMetadataList(List.of(DocumentMetadata.create()
				.withKey("key")
				.withValue("value"))));

		when(documentServiceMock.create(any(), any())).thenReturn(Document.create());

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
		verify(documentServiceMock).create(any(DocumentCreateRequest.class), any(MultipartFile.class));
	}

	@Test
	void update() {

		// Arrange
		final var registrationNumber = "2023-1337";
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("documentFile", "file-content").filename("test.txt").contentType(TEXT_PLAIN);
		multipartBodyBuilder.part("document", DocumentUpdateRequest.create()
			.withCreatedBy("user")
			.withMetadataList(List.of(DocumentMetadata.create()
				.withKey("key")
				.withValue("value"))));

		when(documentServiceMock.update(any(), any(), any())).thenReturn(Document.create());

		// Act
		final var response = webTestClient.patch()
			.uri("/documents/" + registrationNumber)
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
		verify(documentServiceMock).update(eq(registrationNumber), any(DocumentUpdateRequest.class), any(MultipartFile.class));
	}

	@Test
	void read() {

		// Arrange
		final var registrationNumber = "2023-1337";

		when(documentServiceMock.read(any())).thenReturn(Document.create());

		// Act
		final var response = webTestClient.get()
			.uri("/documents/" + registrationNumber)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		verify(documentServiceMock).read(registrationNumber);
	}

	@Test
	void readFile() {

		// Arrange
		final var registrationNumber = "2023-1337";

		// Act
		final var response = webTestClient.get()
			.uri("/documents/" + registrationNumber + "/file")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNull();
		verify(documentServiceMock).readFile(eq(registrationNumber), any(HttpServletResponse.class));
	}
}
