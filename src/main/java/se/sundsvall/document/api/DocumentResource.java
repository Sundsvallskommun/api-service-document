package se.sundsvall.document.api;

import static jakarta.validation.Validation.buildDefaultValidatorFactory;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;

import java.util.List;
import java.util.Set;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.document.api.model.ConfidentialityUpdateRequest;
import se.sundsvall.document.api.model.Document;
import se.sundsvall.document.api.model.DocumentCreateRequest;
import se.sundsvall.document.api.model.DocumentDataCreateRequest;
import se.sundsvall.document.api.model.DocumentFiles;
import se.sundsvall.document.api.model.DocumentUpdateRequest;
import se.sundsvall.document.api.model.PagedDocumentResponse;
import se.sundsvall.document.service.DocumentService;

@RestController
@Validated
@RequestMapping("/documents")
@Tag(name = "Documents", description = "Document operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {Problem.class, ConstraintViolationProblem.class})))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
public class DocumentResource {

	private final DocumentService documentService;
	private final ObjectMapper objectMapper;

	public DocumentResource(DocumentService documentService, ObjectMapper objectMapper) {
		this.documentService = documentService;
		this.objectMapper = objectMapper;
	}

	@PostMapping(consumes = {MULTIPART_FORM_DATA_VALUE}, produces = {ALL_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	@Operation(summary = "Create document.")
	@ApiResponse(responseCode = "201", headers = @Header(name = LOCATION, schema = @Schema(type = "string")), description = "Successful operation", useReturnTypeSchema = true)
	public ResponseEntity<Void> create(
		@RequestPart("document") @Schema(description = "Document", implementation = DocumentCreateRequest.class) String documentString,
		@RequestPart(value = "documentFiles") List<MultipartFile> documentFiles) throws JsonProcessingException {

		// If parameter isn't a String an exception (bad content type) will be thrown. Manual deserialization is necessary.
		final var documentCreateRequest = objectMapper.readValue(documentString, DocumentCreateRequest.class);
		validate(documentCreateRequest);

		final var documents = DocumentFiles.create().withFiles(documentFiles);
		validate(documents);

		final var registrationNumber = documentService.create(documentCreateRequest, documents).getRegistrationNumber();

		return created(fromPath("/documents/{registrationNumber}").buildAndExpand(registrationNumber).toUri())
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}

	@PatchMapping(path = "/{registrationNumber}", consumes = {APPLICATION_JSON_VALUE}, produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	@Operation(summary = "Update document.")
	@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true)
	@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<Document> update(
		@Parameter(name = "registrationNumber", description = "Document registration number", example = "2023-2281-1337") @PathVariable("registrationNumber") String registrationNumber,
		@Parameter(name = "includeConfidential", description = "Include confidential records", example = "true") @RequestParam(name = "includeConfidential", defaultValue = "false") boolean includeConfidential,
		@NotNull @Valid @RequestBody DocumentUpdateRequest body) {

		return ok(documentService.update(registrationNumber, includeConfidential, body));
	}

	@PatchMapping(path = "/{registrationNumber}/confidentiality", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	@Operation(summary = "Update document confidentiality (on all revisions).")
	@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true)
	@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<Void> updateConfidentiality(
		@Parameter(name = "registrationNumber", description = "Document registration number", example = "2023-2281-1337") @PathVariable("registrationNumber") String registrationNumber,
		@NotNull @Valid @RequestBody ConfidentialityUpdateRequest body) {

		documentService.updateConfidentiality(registrationNumber, body);
		return noContent().build();
	}

	@GetMapping(path = "/{registrationNumber}", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	@Operation(summary = "Read document (latest revision).")
	@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true)
	@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<Document> read(
		@Parameter(name = "registrationNumber", description = "Document registration number", example = "2023-2281-1337") @PathVariable("registrationNumber") String registrationNumber,
		@Parameter(name = "includeConfidential", description = "Include confidential records", example = "true") @RequestParam(name = "includeConfidential", defaultValue = "false") boolean includeConfidential) {

		return ok(documentService.read(registrationNumber, includeConfidential));
	}

	@GetMapping(path = "/{registrationNumber}/files/{documentDataId}", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	@Operation(summary = "Read document file (latest revision).")
	@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true)
	@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<Void> readFile(
		HttpServletResponse response,
		@Parameter(name = "registrationNumber", description = "Document registration number", example = "2023-2281-1337") @PathVariable("registrationNumber") String registrationNumber,
		@Parameter(name = "documentDataId", description = "Document data ID", example = "082ba08f-03c7-409f-b8a6-940a1397ba38") @PathVariable("documentDataId") @ValidUuid String documentDataId,
		@Parameter(name = "includeConfidential", description = "Include confidential records", example = "true") @RequestParam(name = "includeConfidential", defaultValue = "false") boolean includeConfidential) {

		documentService.readFile(registrationNumber, documentDataId, includeConfidential, response);
		return ok().build();
	}

	@PutMapping(path = "/{registrationNumber}/files", consumes = {MULTIPART_FORM_DATA_VALUE}, produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	@Operation(summary = "Add document file data (or replace existing if filename already exists on the document object)")
	@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true)
	@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<Void> addOrReplaceFile(
		@Parameter(name = "registrationNumber", description = "Document registration number", example = "2023-2281-1337") @PathVariable("registrationNumber") String registrationNumber,
		@RequestPart("document") @Schema(description = "Document", implementation = DocumentDataCreateRequest.class) String documentDataString,
		@RequestPart(value = "documentFile") MultipartFile documentFile) throws JsonProcessingException {

		// If parameter isn't a String an exception (bad content type) will be thrown. Manual deserialization is necessary.
		final var documentDataCreateRequest = objectMapper.readValue(documentDataString, DocumentDataCreateRequest.class);
		validate(documentDataCreateRequest);

		documentService.addOrReplaceFile(registrationNumber, documentDataCreateRequest, documentFile);

		return noContent().build();
	}

	@DeleteMapping(path = "/{registrationNumber}/files/{documentDataId}", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	@Operation(summary = "Delete document file.")
	@ApiResponse(responseCode = "204", description = "Successful operation", useReturnTypeSchema = true)
	@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<Void> deleteFile(
		@Parameter(name = "registrationNumber", description = "Document registration number", example = "2023-2281-1337") @PathVariable("registrationNumber") String registrationNumber,
		@Parameter(name = "documentDataId", description = "Document data ID", example = "082ba08f-03c7-409f-b8a6-940a1397ba38") @PathVariable("documentDataId") @ValidUuid String documentDataId) {

		documentService.deleteFile(registrationNumber, documentDataId);
		return noContent().build();
	}

	@GetMapping(produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	@Operation(summary = "Search documents.")
	@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true)
	public ResponseEntity<PagedDocumentResponse> search(
		@Parameter(name = "query", description = "Search query. Use asterisk-character [*] as wildcard.", example = "hello*") @RequestParam(value = "query", required = true) @NotBlank String query,
		@Parameter(name = "includeConfidential", description = "Include confidential records", example = "true") @RequestParam(name = "includeConfidential", defaultValue = "false") boolean includeConfidential,
		@ParameterObject Pageable pageable) {

		return ok(documentService.search(query, includeConfidential, pageable));
	}

	private <T> void validate(T t) {
		final var validator = buildDefaultValidatorFactory().getValidator();
		final Set<ConstraintViolation<T>> violations = validator.validate(t);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
	}
}
