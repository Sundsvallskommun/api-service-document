package se.sundsvall.document.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

import org.apache.commons.io.IOUtils;
import org.hibernate.LobHelper;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
class DatabaseHelperTest {

	@Mock
	private EntityManager entityManager;

	@Mock
	private Session session;

	@Mock
	private LobHelper lobHelper;

	@Mock
	private Blob blob;

	@InjectMocks
	private DatabaseHelper databaseHelper;

	@Test
	void convertToBlob() throws IOException, SQLException {

		// Arrange
		when(session.getLobHelper()).thenReturn(lobHelper);
		when(entityManager.unwrap(Session.class)).thenReturn(session);
		when(lobHelper.createBlob(any(), anyLong())).thenReturn(blob);

		final var file = new File("src/test/resources/files/image.png");
		final var multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", IOUtils.toByteArray(new FileInputStream(file)));

		// Act
		final var result = databaseHelper.convertToBlob(multipartFile);

		// Assert
		assertThat(result).isNotNull();
		verify(entityManager).unwrap(Session.class);
		verify(session).getLobHelper();
		verify(lobHelper).createBlob(any(InputStream.class), eq(multipartFile.getSize()));
	}

	@Test
	void convertToBlobWhenExceptionThrown() throws IOException, SQLException {

		// Arrange
		when(entityManager.unwrap(Session.class)).thenThrow(new RuntimeException("An error occured"));

		final var file = new File("src/test/resources/files/image.png");
		final var multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", IOUtils.toByteArray(new FileInputStream(file)));

		// Act
		final var exception = assertThrows(RuntimeException.class, () -> databaseHelper.convertToBlob(multipartFile));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("An error occured");
		verify(entityManager).unwrap(Session.class);
		verifyNoInteractions(session, lobHelper);
	}

	@Test
	void convertToBlobWhenNull() throws IOException, SQLException {

		// Act
		final var result = databaseHelper.convertToBlob(null);

		// Assert
		assertThat(result).isNull();
		verifyNoInteractions(entityManager, session, lobHelper);
	}
}
