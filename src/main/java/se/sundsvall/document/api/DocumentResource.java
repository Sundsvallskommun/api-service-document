package se.sundsvall.document.api;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import se.sundsvall.document.api.model.DocumentHeader;

@RestController
@Validated
@RequestMapping("/documents")
@Tag(name = "Documents", description = "Document operations")
public class DocumentResource {

	@Autowired
	private ObjectMapper mapper;

	@PostMapping(consumes = { MULTIPART_FORM_DATA_VALUE }, produces = { ALL_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@Operation(summary = "Create document.")
	@ApiResponse(responseCode = "201", headers = @Header(name = LOCATION, schema = @Schema(type = "string")), description = "Successful operation", useReturnTypeSchema = true)
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
	@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<Void> create(
		final UriComponentsBuilder uriComponentsBuilder,
		@Valid @RequestPart("documentHeader") @Schema(description = "ID of the document", implementation = DocumentHeader.class) String documentHeaderString,
		@RequestPart(value = "document") MultipartFile document) throws JsonProcessingException {

		final var documentHeader = mapper.readValue(documentHeaderString, DocumentHeader.class); // If parameter isn't a String an exception (bad content type) will be thrown. Manual deserialization is necessary.
		validate(documentHeader);

		// TODO: Call service layer.
		final var id = documentHeader.getId();

		return created(uriComponentsBuilder.path("/documents/{id}").buildAndExpand(id).toUri()).header(CONTENT_TYPE, ALL_VALUE).build();
	}

	@PatchMapping(path = "/{registrationNumber}", consumes = { MULTIPART_FORM_DATA_VALUE }, produces = { ALL_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@Operation(summary = "Update document.")
	@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true)
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
	@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<DocumentHeader> update(
		@Parameter(name = "registrationNumber", description = "Document registration number", example = "2023-1337") @PathVariable("registrationNumber") String registrationNumber,
		@Valid @RequestPart(value = "documentHeader", required = false) @Schema(description = "ID of the document", implementation = DocumentHeader.class) String documentHeaderString,
		@RequestPart(value = "document", required = false) MultipartFile document) throws JsonProcessingException {

		final var documentHeader = mapper.readValue(documentHeaderString, DocumentHeader.class); // If parameter isn't a String an exception (bad content type) will be thrown. Manual deserialization is necessary.
		validate(documentHeader);

		// TODO: Call service layer.
		return ok(DocumentHeader.create());
	}

	@GetMapping(path = "/{registrationNumber}", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@Operation(summary = "Read document (latest revision).")
	@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true)
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
	@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<DocumentHeader> read(
		@Parameter(name = "registrationNumber", description = "Document registration number", example = "2023-1337") @PathVariable("registrationNumber") String registrationNumber) {

		// TODO: Call service layer.
		return ok(DocumentHeader.create());
	}

	@GetMapping(path = "/{registrationNumber}/file", produces = { APPLICATION_PROBLEM_JSON_VALUE })
	@Operation(summary = "Read document file (latest revision).")
	@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true)
	@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
	@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<Void> readFile(
		@Parameter(name = "registrationNumber", description = "Document registration number", example = "2023-1337") @PathVariable("registrationNumber") String registrationNumber,
		HttpServletResponse response) {

		// TODO: Call service layer.
		return ok().build();
	}

	private <T> void validate(T t) {
		final var validator = Validation.buildDefaultValidatorFactory().getValidator();
		final Set<ConstraintViolation<T>> violations = validator.validate(t);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
	}
}
