package se.sundsvall.document.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.document.Application;
import se.sundsvall.document.api.model.DocumentType;
import se.sundsvall.document.api.model.DocumentTypeCreateRequest;
import se.sundsvall.document.api.model.DocumentTypeUpdateRequest;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class DocumentTypeAdministrationResourceTest {

	private static final String BASE_PATH = "/{municipalityId}/admin/documenttypes";

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void create() {
		// Arrange

		// Act
		webTestClient.post()
			.uri(uribuilder -> uribuilder.path(BASE_PATH).build(Map.of("municipalityId", "2281")))
			.contentType(APPLICATION_JSON)
			.bodyValue(DocumentTypeCreateRequest.create()
				.withType("type")
				.withDisplayName("displayName")
				.withCreatedBy("createdBy"))
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(ALL_VALUE)
			.expectHeader().valuesMatch(LOCATION, "^/2281/admin/documenttypes/type$")
			.expectBody().isEmpty();

		// Assert and verify
		// TODO: Verify interactions with service mock
	}

	@Test
	void update() {
		// Arrange

		// Act
		webTestClient.patch()
			.uri(uribuilder -> uribuilder.path(BASE_PATH + "/type").build(Map.of("municipalityId", "2281")))
			.contentType(APPLICATION_JSON)
			.bodyValue(DocumentTypeUpdateRequest.create()
				.withDisplayName("displayName")
				.withUpdatedBy("updatedBy"))
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE)
			.expectBody().isEmpty();

		// Assert and verify
		// TODO: Verify interactions with service mock
	}

	@Test
	void readAll() {
		// Act
		final var response = webTestClient.get()
			.uri(uribuilder -> uribuilder.path(BASE_PATH).build(Map.of("municipalityId", "2281")))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBodyList(DocumentType.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isEmpty();
		// TODO: Verify interactions with service mock
	}

	@Test
	void readOne() {
		// Act
		final var response = webTestClient.get()
			.uri(uribuilder -> uribuilder.path(BASE_PATH + "/type").build(Map.of("municipalityId", "2281")))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(DocumentType.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).hasAllNullFieldsOrProperties();
		// TODO: Verify interactions with service mock
	}

	@Test
	void delete() {
		// Act
		webTestClient.delete()
			.uri(uribuilder -> uribuilder.path(BASE_PATH + "/type").build(Map.of("municipalityId", "2281")))
			.exchange()
			.expectStatus().isNoContent()
			.expectHeader().contentType(ALL_VALUE)
			.expectBody().isEmpty();

		// Assert and verify
		// TODO: Verify interactions with service mock
	}
}
