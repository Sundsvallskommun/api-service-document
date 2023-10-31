package se.sundsvall.document.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Min;
import se.sundsvall.document.api.model.Document;
import se.sundsvall.document.api.model.PagedDocumentResponse;
import se.sundsvall.document.service.DocumentService;

@RestController
@Validated
@RequestMapping("/documents/{registrationNumber}/revisions")
@Tag(name = "Document revisions", description = "Document revision operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = { Problem.class, ConstraintViolationProblem.class })))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
public class DocumentRevisionResource {

	private final DocumentService documentService;

	public DocumentRevisionResource(DocumentService documentService) {
		this.documentService = documentService;
	}

	@GetMapping(produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@Operation(summary = "Read document revisions.")
	@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true)
	public ResponseEntity<PagedDocumentResponse> readRevisions(
		@Parameter(name = "registrationNumber", description = "Document registration number", example = "2023-2281-1337") @PathVariable("registrationNumber") String registrationNumber,
		@ParameterObject Pageable pageable) {

		return ok(documentService.readAll(registrationNumber, pageable));
	}

	@GetMapping(path = "/{revision}", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@Operation(summary = "Read document revision.")
	@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true)
	@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<Document> readRevision(
		@Parameter(name = "registrationNumber", description = "Document registration number", example = "2023-2281-1337") @PathVariable("registrationNumber") String registrationNumber,
		@Parameter(name = "revision", description = "Document revision", example = "2") @Min(0) @PathVariable("revision") int revision) {

		return ok(documentService.read(registrationNumber, revision));
	}

	@GetMapping(path = "/{revision}/file", produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	@Operation(summary = "Read document file revision.")
	@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true)
	@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	public ResponseEntity<Void> readFileRevision(
		HttpServletResponse response,
		@Parameter(name = "registrationNumber", description = "Document registration number", example = "2023-2281-1337") @PathVariable("registrationNumber") String registrationNumber,
		@Parameter(name = "revision", description = "Document revision", example = "2") @Min(0) @PathVariable("revision") int revision) {

		documentService.readFile(registrationNumber, revision, response);
		return ok().build();
	}
}
