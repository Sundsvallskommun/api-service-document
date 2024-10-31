package se.sundsvall.document.api;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.web.reactive.function.BodyInserters.fromMultipartData;
import static org.zalando.problem.Status.BAD_REQUEST;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;

import se.sundsvall.document.Application;
import se.sundsvall.document.api.model.Confidentiality;
import se.sundsvall.document.api.model.ConfidentialityUpdateRequest;
import se.sundsvall.document.api.model.Document;
import se.sundsvall.document.api.model.DocumentCreateRequest;
import se.sundsvall.document.api.model.DocumentDataCreateRequest;
import se.sundsvall.document.api.model.DocumentMetadata;
import se.sundsvall.document.api.model.DocumentUpdateRequest;
import se.sundsvall.document.api.validation.DocumentTypeValidator;
import se.sundsvall.document.service.DocumentService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class DocumentResourceFailuresTest {

	@MockBean
	private DocumentService documentServiceMock;

	@MockBean
	private DocumentTypeValidator validationUtilityMock;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void createWithMissingDocumentFiles() {

		// Arrange
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("document", DocumentCreateRequest.create());

		// Act
		final var response = webTestClient.post()
			.uri("/2281/documents")
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
		assertThat(response.getDetail()).isEqualTo("Required part 'documentFiles' is not present.");

		verifyNoInteractions(documentServiceMock);
	}

	@Test
	void createWithDuplicateFileNames() {
		final var documentCreateRequest = DocumentCreateRequest.create()
			.withConfidentiality(Confidentiality.create().withConfidential(true).withLegalCitation("legalCitation"))
			.withCreatedBy("user")
			.withDescription("description")
			.withType("type")
			.withMetadataList(List.of(DocumentMetadata.create()
				.withKey("key")
				.withValue("value")));

		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("documentFiles", "file-content").filename("duplicateName.txt").contentType(TEXT_PLAIN);
		multipartBodyBuilder.part("documentFiles", "file-content").filename("duplicateName.txt").contentType(TEXT_PLAIN);
		multipartBodyBuilder.part("document", documentCreateRequest);

		when(documentServiceMock.create(any(), any(), any())).thenReturn(Document.create());

		final var response = webTestClient.post()
			.uri("/2281/documents")
			.contentType(MULTIPART_FORM_DATA)
			.body(fromMultipartData(multipartBodyBuilder.build()))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).satisfies(problem -> {
			assertThat(problem.getTitle()).isEqualTo("Constraint Violation");
			assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST);
			assertThat(problem.getViolations()).extracting("field", "message")
				.containsExactlyInAnyOrder(tuple("files", "no duplicate file names allowed in the list of files"));
		});

		verifyNoInteractions(documentServiceMock);
	}

	@Test
	void createWithMissingDocument() {

		// Arrange
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("documentFiles", "file-content").filename("test.txt").contentType(TEXT_PLAIN);

		// Act
		final var response = webTestClient.post()
			.uri("/2281/documents")
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

		verifyNoInteractions(documentServiceMock);
	}

	@Test
	void createWithMissingDescriptionAndType() {

		// Arrange
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("documentFiles", "file-content").filename("test.txt").contentType(TEXT_PLAIN);
		multipartBodyBuilder.part("document", DocumentCreateRequest.create()
			.withCreatedBy("user")
			.withMetadataList(List.of(DocumentMetadata.create()
				.withKey("key")
				.withValue("value"))));

		// Act
		final var response = webTestClient.post()
			.uri("/2281/documents")
			.contentType(MULTIPART_FORM_DATA)
			.body(fromMultipartData(multipartBodyBuilder.build()))
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
			.containsExactlyInAnyOrder(
				tuple("description", "must not be blank"),
				tuple("type", "must not be blank"));

		verifyNoInteractions(documentServiceMock);
	}

	@Test
	void createWithTooLongDescription() {

		// Arrange
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("documentFiles", "file-content").filename("test.txt").contentType(TEXT_PLAIN);
		multipartBodyBuilder.part("document", DocumentCreateRequest.create()
			.withDescription(repeat("x", 8193)) // 8192 is max length on description.
			.withCreatedBy("user")
			.withType("type")
			.withMetadataList(List.of(DocumentMetadata.create()
				.withKey("key")
				.withValue("value"))));

		// Act
		final var response = webTestClient.post()
			.uri("/2281/documents")
			.contentType(MULTIPART_FORM_DATA)
			.body(fromMultipartData(multipartBodyBuilder.build()))
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
			.containsExactlyInAnyOrder(tuple("description", "size must be between 0 and 8192"));

		verifyNoInteractions(documentServiceMock);
	}

	@Test
	void createWithEmptyMetadata() {

		// Arrange
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("documentFiles", "file-content").filename("test.txt").contentType(TEXT_PLAIN);
		multipartBodyBuilder.part("document", DocumentCreateRequest.create()
			.withCreatedBy("user")
			.withDescription("description")
			.withType("type")
			.withMetadataList(emptyList()));

		// Act
		final var response = webTestClient.post()
			.uri("/2281/documents")
			.contentType(MULTIPART_FORM_DATA)
			.body(fromMultipartData(multipartBodyBuilder.build()))
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
			.containsExactlyInAnyOrder(tuple("metadataList", "must not be empty"));

		verifyNoInteractions(documentServiceMock);
	}

	@Test
	void createWithMissingMetadata() {

		// Arrange
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("documentFiles", "file-content").filename("test.txt").contentType(TEXT_PLAIN);
		multipartBodyBuilder.part("document", DocumentCreateRequest.create()
			.withDescription("description")
			.withType("type")
			.withCreatedBy("user"));

		// Act
		final var response = webTestClient.post()
			.uri("/2281/documents")
			.contentType(MULTIPART_FORM_DATA)
			.body(fromMultipartData(multipartBodyBuilder.build()))
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
			.containsExactlyInAnyOrder(tuple("metadataList", "must not be empty"));

		verifyNoInteractions(documentServiceMock);
	}

	@Test
	void createWithInvalidType() {
		doThrow(new ConstraintViolationProblem(BAD_REQUEST, List.of(new Violation("type", "error")))).when(validationUtilityMock).validate(any(), any());
		final var documentCreateRequest = DocumentCreateRequest.create()
			.withConfidentiality(Confidentiality.create().withConfidential(true).withLegalCitation("legalCitation"))
			.withCreatedBy("user")
			.withDescription("description")
			.withMetadataList(List.of(DocumentMetadata.create()
				.withKey("key")
				.withValue("value")))
			.withType("type");
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("documentFiles", "file-content").filename("test1.txt").contentType(TEXT_PLAIN);
		multipartBodyBuilder.part("documentFiles", "file-content").filename("tesst2.txt").contentType(TEXT_PLAIN);
		multipartBodyBuilder.part("document", documentCreateRequest);

		// Act
		final var response = webTestClient.post()
			.uri("/2281/documents")
			.contentType(MULTIPART_FORM_DATA)
			.body(fromMultipartData(multipartBodyBuilder.build()))
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
			.containsExactlyInAnyOrder(tuple("type", "error"));

		verifyNoInteractions(documentServiceMock);
	}

	@Test
	void createWithInvalidMunicipalityId() {

		// Arrange
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("documentFiles", "file-content").filename("test.txt").contentType(TEXT_PLAIN);
		multipartBodyBuilder.part("document", DocumentCreateRequest.create()
			.withCreatedBy("user")
			.withDescription("description")
			.withMetadataList(List.of(DocumentMetadata.create()
				.withKey("key")
				.withValue("value"))));

		// Act
		final var response = webTestClient.post()
			.uri("/666/documents") // Invalid municipalityId
			.contentType(MULTIPART_FORM_DATA)
			.body(fromMultipartData(multipartBodyBuilder.build()))
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
			.containsExactlyInAnyOrder(tuple("create.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(documentServiceMock);
	}

	@Test
	void updateWithBlankKeyInMetaData() {

		// Arrange
		final var requestBody = DocumentUpdateRequest.create()
			.withCreatedBy("user")
			.withDescription("description")
			.withMetadataList(List.of(
				DocumentMetadata.create()
					.withKey(" ")
					.withValue("value")));

		// Act
		final var response = webTestClient.patch()
			.uri("/2281/documents/2023-1337")
			.contentType(APPLICATION_JSON)
			.bodyValue(requestBody)
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
			.containsExactlyInAnyOrder(tuple("metadataList[0].key", "must not be blank"));

		verifyNoInteractions(documentServiceMock);
	}

	@Test
	void updateWithBlankValueInMetaData() {

		// Arrange
		final var requestBody = DocumentUpdateRequest.create()
			.withCreatedBy("user")
			.withDescription("description")
			.withMetadataList(List.of(
				DocumentMetadata.create()
					.withKey("key")
					.withValue(" ")));

		// Act
		final var response = webTestClient.patch()
			.uri("/2281/documents/2023-1337")
			.contentType(APPLICATION_JSON)
			.bodyValue(requestBody)
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
			.containsExactlyInAnyOrder(tuple("metadataList[0].value", "must not be blank"));

		verifyNoInteractions(documentServiceMock);
	}

	@Test
	void updateWithMissingCreatedBy() {

		// Arrange
		final var requestBody = DocumentUpdateRequest.create()
			.withDescription("description")
			.withMetadataList(List.of(
				DocumentMetadata.create()
					.withKey("key")
					.withValue("value")));

		// Act
		final var response = webTestClient.patch()
			.uri("/2281/documents/2023-1337")
			.contentType(APPLICATION_JSON)
			.bodyValue(requestBody)
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
			.containsExactlyInAnyOrder(tuple("createdBy", "must not be blank"));

		verifyNoInteractions(documentServiceMock);
	}

	@Test
	void updateWithTooLongDescription() {

		// Arrange
		final var requestBody = DocumentUpdateRequest.create()
			.withCreatedBy("user")
			.withDescription(repeat("x", 8193)) // 8192 is max length on description.
			.withMetadataList(List.of(
				DocumentMetadata.create()
					.withKey("key")
					.withValue("value")));

		// Act
		final var response = webTestClient.patch()
			.uri("/2281/documents/2023-1337")
			.contentType(APPLICATION_JSON)
			.bodyValue(requestBody)
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
			.containsExactlyInAnyOrder(tuple("description", "size must be between 0 and 8192"));

		verifyNoInteractions(documentServiceMock);
	}

	@Test
	void updateWithInvalidType() {
		doThrow(new ConstraintViolationProblem(BAD_REQUEST, List.of(new Violation("type", "error")))).when(validationUtilityMock).validate(any(), any());
		final var requestBody = DocumentUpdateRequest.create()
			.withCreatedBy("user")
			.withType("type")
			.withMetadataList(List.of(DocumentMetadata.create()
				.withKey("key")
				.withValue("value")));

		// Act
		final var response = webTestClient.patch()
			.uri("/2281/documents/2023-1337")
			.contentType(APPLICATION_JSON)
			.bodyValue(requestBody)
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
			.containsExactlyInAnyOrder(tuple("type", "error"));

		verifyNoInteractions(documentServiceMock);
	}

	@Test
	void updateConfidentialityWithMissingValue() {

		// Arrange
		final var requestBody = ConfidentialityUpdateRequest.create()
			.withChangedBy("user");

		// Act
		final var response = webTestClient.patch()
			.uri("/2281/documents/2023-1337/confidentiality")
			.contentType(APPLICATION_JSON)
			.bodyValue(requestBody)
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
			.containsExactlyInAnyOrder(tuple("confidential", "must not be null"));

		verifyNoInteractions(documentServiceMock);
	}

	@Test
	void updateConfidentialityWithBlankChangedBy() {

		// Arrange
		final var requestBody = ConfidentialityUpdateRequest.create()
			.withChangedBy(" ")
			.withConfidential(true)
			.withLegalCitation("Lorum ipsum");

		// Act
		final var response = webTestClient.patch()
			.uri("/2281/documents/2023-1337/confidentiality")
			.contentType(APPLICATION_JSON)
			.bodyValue(requestBody)
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
			.containsExactlyInAnyOrder(tuple("changedBy", "must not be blank"));

		verifyNoInteractions(documentServiceMock);
	}

	@Test
	void searchWithMissingQuery() {

		// Act
		final var response = webTestClient.get()
			.uri("/2281/documents")
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getDetail()).isEqualTo("Required request parameter 'query' for method parameter type String is not present");

		verifyNoInteractions(documentServiceMock);
	}

	@Test
	void searchWithBlankQuery() {

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/2281/documents")
				.queryParam("query", " ")
				.build())
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
			.containsExactlyInAnyOrder(tuple("search.query", "must not be blank"));

		verifyNoInteractions(documentServiceMock);
	}

	@Test
	void readFileWithInvalidDocumentDataId() {

		// Arrange
		final var documentDataId = "not-a-valid-uuid";

		// Act
		final var response = webTestClient.get()
			.uri("/2281/documents/2023-1337/files/" + documentDataId)
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
			.containsExactlyInAnyOrder(tuple("readFile.documentDataId", "not a valid UUID"));

		verifyNoInteractions(documentServiceMock);
	}

	@Test
	void deleteFileWithInvalidDocumentDataId() {

		// Arrange
		final var documentDataId = "not-a-valid-uuid";

		// Act
		final var response = webTestClient.delete()
			.uri("/2281/documents/2023-1337/files/" + documentDataId)
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
			.containsExactlyInAnyOrder(tuple("deleteFile.documentDataId", "not a valid UUID"));

		verifyNoInteractions(documentServiceMock);
	}

	@Test
	void addFileWithBlankCreatedBy() {

		// Arrange
		final var registrationNumber = "2023-1337";

		// Arrange
		final var documentDataCreateRequest = DocumentDataCreateRequest.create()
			.withCreatedBy(" ");
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("documentFile", "file-content").filename("test1.txt").contentType(TEXT_PLAIN);
		multipartBodyBuilder.part("document", documentDataCreateRequest);

		// Act
		final var response = webTestClient.put()
			.uri("/2281/documents/" + registrationNumber + "/files")
			.contentType(MULTIPART_FORM_DATA)
			.body(fromMultipartData(multipartBodyBuilder.build()))
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
			.containsExactlyInAnyOrder(tuple("createdBy", "must not be blank"));

		verifyNoInteractions(documentServiceMock);
	}

}
