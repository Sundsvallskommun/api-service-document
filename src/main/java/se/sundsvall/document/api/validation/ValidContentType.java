package se.sundsvall.document.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({
	ElementType.FIELD, ElementType.PARAMETER
})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidContentTypeConstraintValidator.class)
public @interface ValidContentType {

	String message() default "content type must not be application/octet-stream";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
