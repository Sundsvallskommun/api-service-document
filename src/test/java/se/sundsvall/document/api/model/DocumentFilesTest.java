package se.sundsvall.document.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class DocumentFilesTest {

	@Test
	void testBean() {
		assertThat(DocumentFiles.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		final var multipartFile = (MultipartFile) new MockMultipartFile("file2", "file123", "text/plain", "empty".getBytes());

		final List<MultipartFile> files = List.of(multipartFile);

		final var bean = DocumentFiles.create().withFiles(files);

		Assertions.assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		Assertions.assertThat(bean.getFiles()).isEqualTo(files);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		Assertions.assertThat(DocumentFiles.create()).hasAllNullFieldsOrProperties();
		Assertions.assertThat(new DocumentFiles()).hasAllNullFieldsOrProperties();
	}

}
