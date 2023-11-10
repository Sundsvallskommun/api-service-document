package se.sundsvall.document.api;

import static jakarta.validation.Validation.buildDefaultValidatorFactory;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;

import java.util.Set;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
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
import jakarta.validation.constraints.NotBlank;
import se.sundsvall.document.api.model.Document;
import se.sundsvall.document.api.model.DocumentCreateRequest;
import se.sundsvall.document.api.model.DocumentUpdateRequest;
import se.sundsvall.document.api.model.PagedDocumentResponse;
import se.sundsvall.document.service.DocumentService;

@RestController
@Validated
@RequestMapping("/documents")
@Tag(name = "Documents", description = "Document operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
public class DocumentResource {

	private final DocumentService documentService;
	private final ObjectMapper objectMapper;

	public DocumentResource(DocumentService documentService, ObjectMapper objectMapper) {
		this.documentService = documentService;
		this.objectMapper = objectMapper;
	}

	@PostMapping(consumes = { MULTIPART_FORM_DATA_VALUE }, produces = { ALL_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@Operation(summary = "Create document.")
	@ApiResponse(responseCode = "201", headers = @Header(name = LOCATION, schema = @Schema(type = "string")), description = "Successful operation", useReturnTypeSchema = true)
	public ResponseEntity<Void> create(
		UriComponentsBuilder uriComponentsBuilder,
		@RequestPart("document") @Schema(description = "Document", implementation = DocumentCreateRequest.class) String documentString,
		@RequestPart(value = "documentFile") MultipartFile documentFile) throws JsonProcessingException {

		final var documentCreateRequest = objectMapper.readValue(documentString, DocumentCreateRequest.class); // If parameter isn't a String an exception (bad content type) will be thrown. Manual deserialization is necessary.
		validate(documentCreateRequest);

		final var registrationNumber = documentService.create(documentCreateRequest, documentFile).getRegistrationNumber();

		return created(uriComponentsBuilder.path("/documents/{registrationNumber}").buildAndExpand(registrationNumber).toUri()).header(CONTENT_TYPE, ALL_VALUE).build();
	}

	@PatchMapping(path = "/{registrationNumber}", consumes = { MULTIPART_FORM_DATA_VALUE }, produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@Operation(summary = "Update document.")
	@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true)
	@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<Document> update(
		@Parameter(name = "registrationNumber", description = "Document registration number", example = "2023-2281-1337") @PathVariable("registrationNumber") String registrationNumber,
		@Parameter(name = "includeConfidential", description = "Include confidential records", example = "true") @RequestParam(name = "includeConfidential", defaultValue = "false") boolean includeConfidential,
		@RequestPart(value = "document", required = false) @Schema(description = "Document", implementation = DocumentUpdateRequest.class) String documentString,
		@RequestPart(value = "documentFile", required = false) MultipartFile documentFile) throws JsonProcessingException {

		final var documentUpdateRequest = objectMapper.readValue(documentString, DocumentUpdateRequest.class); // If parameter isn't a String an exception (bad content type) will be thrown. Manual deserialization is necessary.
		validate(documentUpdateRequest);

		return ok(documentService.update(registrationNumber, includeConfidential, documentUpdateRequest, documentFile));
	}

	@GetMapping(path = "/{registrationNumber}", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@Operation(summary = "Read document (latest revision).")
	@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true)
	@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<Document> read(
		@Parameter(name = "registrationNumber", description = "Document registration number", example = "2023-2281-1337") @PathVariable("registrationNumber") String registrationNumber,
		@Parameter(name = "includeConfidential", description = "Include confidential records", example = "true") @RequestParam(name = "includeConfidential", defaultValue = "false") boolean includeConfidential) {

		return ok(documentService.read(registrationNumber, includeConfidential));
	}

	@GetMapping(produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@Operation(summary = "Search documents.")
	@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true)
	public ResponseEntity<PagedDocumentResponse> search(
		@Parameter(name = "query", description = "Search query. Use asterisk-character [*] as wildcard.", example = "hello*") @RequestParam(value = "query", required = true) @NotBlank String query,
		@Parameter(name = "includeConfidential", description = "Include confidential records", example = "true") @RequestParam(name = "includeConfidential", defaultValue = "false") boolean includeConfidential,
		@ParameterObject Pageable pageable) {

		return ok(documentService.search(query, includeConfidential, pageable));
	}

	@GetMapping(path = "/{registrationNumber}/file", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@Operation(summary = "Read document file (latest revision).")
	@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true)
	@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<Void> readFile(
		HttpServletResponse response,
		@Parameter(name = "registrationNumber", description = "Document registration number", example = "2023-2281-1337") @PathVariable("registrationNumber") String registrationNumber,
		@Parameter(name = "includeConfidential", description = "Include confidential records", example = "true") @RequestParam(name = "includeConfidential", defaultValue = "false") boolean includeConfidential) {

		documentService.readFile(registrationNumber, includeConfidential, response);
		return ok().build();
	}

	private <T> void validate(T t) {
		final var validator = buildDefaultValidatorFactory().getValidator();
		final Set<ConstraintViolation<T>> violations = validator.validate(t);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
	}
}
