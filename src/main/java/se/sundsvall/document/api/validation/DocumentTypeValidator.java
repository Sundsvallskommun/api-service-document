package se.sundsvall.document.api.validation;

import java.util.List;
import org.springframework.stereotype.Component;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import se.sundsvall.document.api.model.DocumentType;
import se.sundsvall.document.service.DocumentTypeService;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.zalando.problem.Status.BAD_REQUEST;

@Component
public class DocumentTypeValidator {

	private final DocumentTypeService documentTypeService;

	DocumentTypeValidator(DocumentTypeService documentTypeService) {
		this.documentTypeService = documentTypeService;
	}

	public void validate(final String municipalityId, final String documentType) {
		if (isNull(documentType)) {
			return;
		}

		final var validTypes = documentTypeService.read(municipalityId).stream().map(DocumentType::getType).toList();

		validTypes.stream()
			.filter(type -> equalsIgnoreCase(type, documentType))
			.findAny()
			.orElseThrow(() -> new ConstraintViolationProblem(BAD_REQUEST, List.of(new Violation("type",
				"document type '%s' must match one of %s".formatted(documentType, validTypes)))));
	}
}
