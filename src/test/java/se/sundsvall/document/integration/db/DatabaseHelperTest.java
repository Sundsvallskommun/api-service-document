package se.sundsvall.document.integration.db;

import static org.apache.commons.io.IOUtils.toByteArray;
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

import org.hibernate.LobHelper;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
class DatabaseHelperTest {

	@Mock
	private EntityManager entityManagerMock;

	@Mock
	private LobHelper lobHelperMock;

	@Mock
	private Blob blobMock;

	@InjectMocks
	private DatabaseHelper databaseHelper;

	@Test
	void convertToBlob() throws IOException {

		// Arrange
		final var sessionMock = Mockito.mock(Session.class);
		when(sessionMock.getLobHelper()).thenReturn(lobHelperMock);
		when(entityManagerMock.unwrap(Session.class)).thenReturn(sessionMock);
		when(lobHelperMock.createBlob(any(), anyLong())).thenReturn(blobMock);

		final var file = new File("src/test/resources/files/image.png");
		final var multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", toByteArray(new FileInputStream(file)));

		// Act
		final var result = databaseHelper.convertToBlob(multipartFile);

		// Assert
		assertThat(result).isNotNull().isInstanceOf(Blob.class);
		verify(entityManagerMock).unwrap(Session.class);
		verify(sessionMock).getLobHelper();
		verify(lobHelperMock).createBlob(any(InputStream.class), eq(multipartFile.getSize()));
	}

	@Test
	void convertToBlobWhenExceptionThrown() throws IOException {

		// Arrange
		when(entityManagerMock.unwrap(any())).thenThrow(new RuntimeException("An error occured"));

		final var file = new File("src/test/resources/files/image.png");
		final var multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", toByteArray(new FileInputStream(file)));

		// Act
		final var exception = assertThrows(RuntimeException.class, () -> databaseHelper.convertToBlob(multipartFile));

		// Assert
		assertThat(exception).isNotNull();
		assertThat(exception.getMessage()).isEqualTo("An error occured");
		verify(entityManagerMock).unwrap(Session.class);
		verifyNoInteractions(lobHelperMock);
	}

	@Test
	void convertToBlobWhenNull() throws IOException, SQLException {

		// Act
		final var result = databaseHelper.convertToBlob(null);

		// Assert
		assertThat(result).isNull();
		verifyNoInteractions(entityManagerMock, lobHelperMock);
	}
}
