package se.sundsvall.document.api;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;
import static se.sundsvall.document.Constants.ADMIN_DOCUMENT_TYPES_BASE_PATH;

import java.util.Collections;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.document.api.model.DocumentType;
import se.sundsvall.document.api.model.DocumentTypeCreateRequest;
import se.sundsvall.document.api.model.DocumentTypeUpdateRequest;

@RestController
@Validated
@RequestMapping(ADMIN_DOCUMENT_TYPES_BASE_PATH)
@Tag(name = "Administration", description = "Administration of document types")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
public class DocumentTypeAdministrationResource {

	DocumentTypeAdministrationResource() {}

	@PostMapping(consumes = { APPLICATION_JSON_VALUE }, produces = { ALL_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@Operation(summary = "Create new document type", description = "Creates a new document type in the provided municipality.")
	@ApiResponse(responseCode = "201", headers = @Header(name = LOCATION, schema = @Schema(type = "string")), description = "Successful operation", useReturnTypeSchema = true)
	ResponseEntity<Void> createDocumentType(
		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @PathVariable("municipalityId") @ValidMunicipalityId String municipalityId,
		@Valid @NotNull @RequestBody final DocumentTypeCreateRequest body) {

		return created(fromPath(ADMIN_DOCUMENT_TYPES_BASE_PATH + "/{type}").buildAndExpand(municipalityId, body.getType()).toUri())
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@GetMapping(produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@Operation(summary = "Get document types", description = "Get all existing document types defined in provided municipality.")
	@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true)
	ResponseEntity<List<DocumentType>> readDocumentTypes(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId) {

		return ok(Collections.emptyList());
	}

	@GetMapping(path = "/{type}", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@Operation(summary = "Get document type", description = "Get document type matching provided type and municipality.")
	@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true)
	ResponseEntity<DocumentType> readDocumentType(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "type", description = "The type to update", example = "EMPLOYMENT_CERTIFICATE") @PathVariable final String type) {

		return ok(DocumentType.create());
	}

	@PatchMapping(path = "/{type}", consumes = APPLICATION_JSON_VALUE, produces = { APPLICATION_PROBLEM_JSON_VALUE })
	@Operation(summary = "Update document type", description = "Updates an existing document type in the provided municipality.")
	@ApiResponse(responseCode = "204", description = "Successful operation", useReturnTypeSchema = true)
	@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	ResponseEntity<Void> updateDocumentType(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "type", description = "The type to update", example = "EMPLOYMENT_CERTIFICATE") @PathVariable final String type,
		@Valid @NotNull @RequestBody final DocumentTypeUpdateRequest body) {

		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@DeleteMapping(path = "/{type}", produces = { APPLICATION_PROBLEM_JSON_VALUE })
	@Operation(summary = "Delete document type", description = """
		Deletes an existing document type in the provided municipality, but only if the type is not used by any existing document. If type is used then an exception will be returned.
		""")
	@ApiResponse(responseCode = "204", description = "Successful operation", useReturnTypeSchema = true)
	@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	ResponseEntity<Void> deleteDocumentType(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "type", description = "the type to delete", example = "EMPLOYMENT_CERTIFICATE") @PathVariable final String type) {

		return noContent()
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}
}
