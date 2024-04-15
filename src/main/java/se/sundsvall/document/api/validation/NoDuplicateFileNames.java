package se.sundsvall.document.api.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoDuplicateFileNamesConstraintValidator.class)
public @interface NoDuplicateFileNames {

	String message() default "no duplicate file names allowed in the list of files";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
