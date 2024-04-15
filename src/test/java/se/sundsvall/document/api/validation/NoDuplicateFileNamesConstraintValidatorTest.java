package se.sundsvall.document.api.validation;

import static org.apache.commons.io.IOUtils.toByteArray;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import jakarta.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import se.sundsvall.document.api.model.DocumentFiles;

@ExtendWith(MockitoExtension.class)
class NoDuplicateFileNamesConstraintValidatorTest {

	@InjectMocks
	private NoDuplicateFileNamesConstraintValidator validator;

	@Mock
	private ConstraintValidatorContext contextMock;

	@Mock
	private ConstraintValidatorContext.ConstraintViolationBuilder builderMock;

	@Test
	void isValid_ValidValues() throws IOException {
		final var file1 = new File("src/test/resources/files/image.png");
		final var file2 = new File("src/test/resources/files/readme.txt");
		final var multipartFile1 = (MultipartFile) new MockMultipartFile("file1", file1.getName(), "image/png", toByteArray(new FileInputStream(file1)));
		final var multipartFile2 = (MultipartFile) new MockMultipartFile("file2", file2.getName(), "text/plain", toByteArray(new FileInputStream(file2)));
		final var documentFiles = DocumentFiles.create().withFiles(List.of(multipartFile1, multipartFile2));

		assertThat(validator.isValid(documentFiles.getFiles(), contextMock)).isTrue();
	}

	@Test
	void isValid_OneDuplicate() throws IOException {
		final var file1 = new File("src/test/resources/files/image.png");
		final var file2 = new File("src/test/resources/files/image.png");
		final var multipartFile1 = (MultipartFile) new MockMultipartFile("file1", file1.getName(), "image/png", toByteArray(new FileInputStream(file1)));
		final var multipartFile2 = (MultipartFile) new MockMultipartFile("file2", file2.getName(), "text/plain", toByteArray(new FileInputStream(file2)));
		final var documentFiles = DocumentFiles.create().withFiles(List.of(multipartFile1, multipartFile2));

		assertThat(validator.isValid(documentFiles.getFiles(), contextMock)).isFalse();
	}

	@Test
	void isValid_BlankFileName() {
		when(contextMock.buildConstraintViolationWithTemplate(anyString())).thenReturn(builderMock);
		when(builderMock.addConstraintViolation()).thenReturn(contextMock);
		final var multipartFile = (MultipartFile) new MockMultipartFile("file", "", "image/png", "empty".getBytes());
		final var documentFiles = DocumentFiles.create().withFiles(List.of(multipartFile));

		assertThat(validator.isValid(documentFiles.getFiles(), contextMock)).isFalse();
	}

}
