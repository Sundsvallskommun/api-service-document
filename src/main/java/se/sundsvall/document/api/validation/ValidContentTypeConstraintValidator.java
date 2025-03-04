package se.sundsvall.document.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public class ValidContentTypeConstraintValidator implements ConstraintValidator<ValidContentType, List<MultipartFile>> {

	@Override
	public boolean isValid(final List<MultipartFile> files, final ConstraintValidatorContext context) {
		return files.stream()
			.map(MultipartFile::getContentType)
			.noneMatch("application/octet-stream"::equals);
	}
}
