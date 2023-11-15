package se.sundsvall.document.api;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.data.domain.Sort.Order.asc;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.web.reactive.function.BodyInserters.fromMultipartData;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import se.sundsvall.document.api.model.PagedDocumentResponse;
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
		final var documentCreateRequest = DocumentCreateRequest.create()
			.withConfidential(true)
			.withCreatedBy("user")
			.withDescription("description")
			.withMunicipalityId("2281")
			.withMetadataList(List.of(DocumentMetadata.create()
				.withKey("key")
				.withValue("value")));
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("documentFiles", "file-content").filename("test1.txt").contentType(TEXT_PLAIN);
		multipartBodyBuilder.part("documentFiles", "file-content").filename("test2.txt").contentType(TEXT_PLAIN);
		multipartBodyBuilder.part("document", documentCreateRequest);

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
		verify(documentServiceMock).create(eq(documentCreateRequest), ArgumentMatchers.<List<MultipartFile>>any());
	}

	@Test
	void update() {

		// Arrange
		final var registrationNumber = "2023-1337";
		final var documentUpdateRequest = DocumentUpdateRequest.create()
			.withCreatedBy("user")
			.withMetadataList(List.of(DocumentMetadata.create()
				.withKey("key")
				.withValue("value")));
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("documentFile", "file-content").filename("test.txt").contentType(TEXT_PLAIN);
		multipartBodyBuilder.part("document", documentUpdateRequest);

		when(documentServiceMock.update(any(), anyBoolean(), any(), any())).thenReturn(Document.create());

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
		verify(documentServiceMock).update(eq(registrationNumber), eq(false), eq(documentUpdateRequest), any(MultipartFile.class));
	}

	@Test
	void updateWithIncludeConfidential() {

		// Arrange
		final var includeConfidential = true;
		final var registrationNumber = "2023-1337";
		final var documentUpdateRequest = DocumentUpdateRequest.create()
			.withCreatedBy("user")
			.withMetadataList(List.of(DocumentMetadata.create()
				.withKey("key")
				.withValue("value")));
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("documentFile", "file-content").filename("test.txt").contentType(TEXT_PLAIN);
		multipartBodyBuilder.part("document", documentUpdateRequest);

		when(documentServiceMock.update(any(), anyBoolean(), any(), any())).thenReturn(Document.create());

		// Act
		final var response = webTestClient.patch()
			.uri(uriBuilder -> uriBuilder.path("/documents/" + registrationNumber)
				.queryParam("includeConfidential", includeConfidential)
				.build())
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
		verify(documentServiceMock).update(eq(registrationNumber), eq(includeConfidential), eq(documentUpdateRequest), any(MultipartFile.class));
	}

	@Test
	void search() {

		// Arrange
		final var query = "string";
		final var page = 1;
		final var size = 10;
		final var sort = "created,asc";

		when(documentServiceMock.search(any(), anyBoolean(), any())).thenReturn(PagedDocumentResponse.create().withDocuments(List.of(Document.create())));

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/documents")
				.queryParam("query", query)
				.queryParam("page", page)
				.queryParam("size", size)
				.queryParam("sort", sort)
				.build())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		verify(documentServiceMock).search(query, false, PageRequest.of(page, size, Sort.by(asc("created"))));
	}

	@Test
	void searchWithIncludeConfidential() {

		// Arrange
		final var includeConfidential = true;
		final var query = "string";
		final var page = 1;
		final var size = 10;
		final var sort = "created,asc";

		when(documentServiceMock.search(any(), anyBoolean(), any())).thenReturn(PagedDocumentResponse.create().withDocuments(List.of(Document.create())));

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/documents")
				.queryParam("query", query)
				.queryParam("page", page)
				.queryParam("size", size)
				.queryParam("sort", sort)
				.queryParam("includeConfidential", includeConfidential)
				.build())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		verify(documentServiceMock).search(query, includeConfidential, PageRequest.of(page, size, Sort.by(asc("created"))));
	}

	@Test
	void read() {

		// Arrange
		final var registrationNumber = "2023-1337";

		when(documentServiceMock.read(any(), anyBoolean())).thenReturn(Document.create());

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
		verify(documentServiceMock).read(registrationNumber, false);
	}

	@Test
	void readWithIncludeConfidential() {

		// Arrange
		final var includeConfidential = true;
		final var registrationNumber = "2023-1337";

		when(documentServiceMock.read(any(), anyBoolean())).thenReturn(Document.create());

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/documents/" + registrationNumber)
				.queryParam("includeConfidential", includeConfidential)
				.build())
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		verify(documentServiceMock).read(registrationNumber, includeConfidential);
	}

	@Test
	void readFile() {

		// Arrange
		final var documentDataId = randomUUID().toString();
		final var registrationNumber = "2023-1337";

		// Act
		webTestClient.get()
			.uri("/documents/" + registrationNumber + "/files/" + documentDataId)
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.isEmpty();

		// Assert
		verify(documentServiceMock).readFile(eq(registrationNumber), eq(documentDataId), eq(false), any(HttpServletResponse.class));
	}

	@Test
	void readFileWithIncludeConfidential() {

		// Arrange
		final var documentDataId = randomUUID().toString();
		final var includeConfidential = true;
		final var registrationNumber = "2023-1337";

		// Act
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/documents/" + registrationNumber + "/files/" + documentDataId)
				.queryParam("includeConfidential", includeConfidential)
				.build())
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.isEmpty();

		// Assert
		verify(documentServiceMock).readFile(eq(registrationNumber), eq(documentDataId), eq(includeConfidential), any(HttpServletResponse.class));
	}

	@Test
	void deleteFile() {

		// Arrange
		final var documentDataId = randomUUID().toString();
		final var registrationNumber = "2023-1337";

		// Act
		webTestClient.delete()
			.uri("/documents/" + registrationNumber + "/files/" + documentDataId)
			.exchange()
			.expectStatus().isNoContent()
			.expectBody()
			.isEmpty();

		// Assert
		verify(documentServiceMock).deleteFile(registrationNumber, documentDataId);
	}
}
