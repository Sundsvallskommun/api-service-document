package se.sundsvall.document.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.violations.ConstraintViolationProblem;

import se.sundsvall.document.Application;
import se.sundsvall.document.api.model.DocumentTypeCreateRequest;
import se.sundsvall.document.api.model.DocumentTypeUpdateRequest;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class DocumentTypeAdministrationResourceFailuresTest {

	private static final String BASE_PATH = "/{municipalityId}/admin/documenttypes";

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void createWithInvalidMunicipalityId() {
		// Act
		final var response = webTestClient.post()
			.uri(uribuilder -> uribuilder.path(BASE_PATH).build(Map.of("municipalityId", "invalid")))
			.contentType(APPLICATION_JSON)
			.bodyValue(DocumentTypeCreateRequest.create()
				.withType("type")
				.withDisplayName("displayName")
				.withCreatedBy("createdBy"))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST);
		assertThat(response.getViolations()).isNotEmpty().satisfiesExactlyInAnyOrder(violation -> {
			assertThat(violation.getField()).isEqualTo("createDocumentType.municipalityId");
			assertThat(violation.getMessage()).isEqualTo("not a valid municipality ID");
		});

		// TODO: Verify no interactions with service mock
		// verifyNoInteractions(serviceMock);
	}

	@Test
	void createWithMissingBody() {
		// Act
		final var response = webTestClient.post()
			.uri(uribuilder -> uribuilder.path(BASE_PATH).build(Map.of("municipalityId", "2281")))
			.contentType(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST);
		assertThat(response.getDetail()).isEqualTo("""
			Required request body is missing: org.springframework.http.ResponseEntity<java.lang.Void> \
			se.sundsvall.document.api.DocumentTypeAdministrationResource.createDocumentType(java.lang.String,se.sundsvall.document.api.model.DocumentTypeCreateRequest)""");

		// TODO: Verify no interactions with service mock
		// verifyNoInteractions(serviceMock);
	}

	@Test
	void createWithEmptyBody() {
		// Act
		final var response = webTestClient.post()
			.uri(uribuilder -> uribuilder.path(BASE_PATH).build(Map.of("municipalityId", "2281")))
			.contentType(APPLICATION_JSON)
			.bodyValue(DocumentTypeCreateRequest.create())
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST);
		assertThat(response.getViolations()).isNotEmpty().satisfiesExactlyInAnyOrder(violation -> {
			assertThat(violation.getField()).isEqualTo("createdBy");
			assertThat(violation.getMessage()).isEqualTo("must not be blank");
		}, violation -> {
			assertThat(violation.getField()).isEqualTo("displayName");
			assertThat(violation.getMessage()).isEqualTo("must not be blank");
		}, violation -> {
			assertThat(violation.getField()).isEqualTo("type");
			assertThat(violation.getMessage()).isEqualTo("must not be blank");
		});

		// TODO: Verify no interactions with service mock
		// verifyNoInteractions(serviceMock);
	}

	@Test
	void readAllWithInvalidMunicipalityId() {
		// Act
		final var response = webTestClient.get()
			.uri(uribuilder -> uribuilder.path(BASE_PATH).build(Map.of("municipalityId", "invalid")))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST);
		assertThat(response.getViolations()).isNotEmpty().satisfiesExactlyInAnyOrder(violation -> {
			assertThat(violation.getField()).isEqualTo("readDocumentTypes.municipalityId");
			assertThat(violation.getMessage()).isEqualTo("not a valid municipality ID");
		});

		// TODO: Verify no interactions with service mock
		// verifyNoInteractions(serviceMock);
	}

	@Test
	void readOneWithInvalidMunicipalityId() {
		// Act
		final var response = webTestClient.get()
			.uri(uribuilder -> uribuilder.path(BASE_PATH + "/type").build(Map.of("municipalityId", "invalid")))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST);
		assertThat(response.getViolations()).isNotEmpty().satisfiesExactlyInAnyOrder(violation -> {
			assertThat(violation.getField()).isEqualTo("readDocumentType.municipalityId");
			assertThat(violation.getMessage()).isEqualTo("not a valid municipality ID");
		});

		// TODO: Verify no interactions with service mock
		// verifyNoInteractions(serviceMock);
	}

	@Test
	void updateWithInvalidMunicipalityId() {
		// Act
		final var response = webTestClient.patch()
			.uri(uribuilder -> uribuilder.path(BASE_PATH + "/type").build(Map.of("municipalityId", "invalid")))
			.contentType(APPLICATION_JSON)
			.bodyValue(DocumentTypeUpdateRequest.create()
				.withDisplayName("displayName")
				.withUpdatedBy("updatedBy"))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST);
		assertThat(response.getViolations()).isNotEmpty().satisfiesExactlyInAnyOrder(violation -> {
			assertThat(violation.getField()).isEqualTo("updateDocumentType.municipalityId");
			assertThat(violation.getMessage()).isEqualTo("not a valid municipality ID");
		});

		// TODO: Verify no interactions with service mock
		// verifyNoInteractions(serviceMock);
	}

	@Test
	void updateWithMissingBody() {
		// Act
		final var response = webTestClient.patch()
			.uri(uribuilder -> uribuilder.path(BASE_PATH + "/type").build(Map.of("municipalityId", "2281")))
			.contentType(APPLICATION_JSON)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST);
		assertThat(response.getDetail()).isEqualTo("""
			Required request body is missing: org.springframework.http.ResponseEntity<java.lang.Void> \
			se.sundsvall.document.api.DocumentTypeAdministrationResource.updateDocumentType(java.lang.String,java.lang.String,se.sundsvall.document.api.model.DocumentTypeUpdateRequest)""");

		// TODO: Verify no interactions with service mock
		// verifyNoInteractions(serviceMock);
	}

	@Test
	void updateWithEmptyBody() {
		// Act
		final var response = webTestClient.patch()
			.uri(uribuilder -> uribuilder.path(BASE_PATH + "/type").build(Map.of("municipalityId", "2281")))
			.contentType(APPLICATION_JSON)
			.bodyValue(DocumentTypeUpdateRequest.create())
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST);
		assertThat(response.getViolations()).isNotEmpty().satisfiesExactlyInAnyOrder(violation -> {
			assertThat(violation.getField()).isEqualTo("updatedBy");
			assertThat(violation.getMessage()).isEqualTo("must not be blank");
		}, violation -> {
			assertThat(violation.getField()).isEqualTo("displayName");
			assertThat(violation.getMessage()).isEqualTo("must not be blank");
		});

		// TODO: Verify no interactions with service mock
		// verifyNoInteractions(serviceMock);
	}

	@Test
	void deleteWithInvalidMunicipalityId() {
		// Act
		final var response = webTestClient.delete()
			.uri(uribuilder -> uribuilder.path(BASE_PATH + "/type").build(Map.of("municipalityId", "invalid")))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST);
		assertThat(response.getViolations()).isNotEmpty().satisfiesExactlyInAnyOrder(violation -> {
			assertThat(violation.getField()).isEqualTo("deleteDocumentType.municipalityId");
			assertThat(violation.getMessage()).isEqualTo("not a valid municipality ID");
		});

		// TODO: Verify no interactions with service mock
		// verifyNoInteractions(serviceMock);
	}

}
