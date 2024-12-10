package se.sundsvall.document.api.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Status;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.document.api.model.DocumentType;
import se.sundsvall.document.service.DocumentTypeService;

@ExtendWith(MockitoExtension.class)
class DocumentTypeValidatorTest {

	@Mock
	private DocumentTypeService documentTypeServiceMock;

	@InjectMocks
	private DocumentTypeValidator validator;

	@Test
	void validateWithNull() {
		// Arrange
		final var municipalityId = "municipalityId";

		// Act and assert
		assertDoesNotThrow(() -> validator.validate(municipalityId, null));
	}

	@Test
	void validateWithValidType() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var type = "type";
		when(documentTypeServiceMock.read(municipalityId)).thenReturn(List.of(DocumentType.create().withType(type)));

		// Act and assert
		assertDoesNotThrow(() -> validator.validate(municipalityId, type));
	}

	@Test
	void validateWithInvalidType() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var type = "type";
		when(documentTypeServiceMock.read(municipalityId)).thenReturn(List.of(DocumentType.create().withType("othertype")));

		// Act and assert
		final var e = assertThrows(ConstraintViolationProblem.class, () -> validator.validate(municipalityId, type));

		assertThat(e.getStatus()).isEqualTo(Status.BAD_REQUEST);
		assertThat(e.getViolations()).hasSize(1).satisfiesExactly(violation -> {
			assertThat(violation.getField()).isEqualTo("type");
			assertThat(violation.getMessage()).isEqualTo("document type 'type' must match one of [othertype]");
		});
	}
}
