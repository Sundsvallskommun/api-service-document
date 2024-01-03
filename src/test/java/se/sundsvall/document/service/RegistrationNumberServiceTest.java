package se.sundsvall.document.service;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.document.integration.db.RegistrationNumberSequenceRepository;
import se.sundsvall.document.integration.db.model.RegistrationNumberSequenceEntity;

@ExtendWith(MockitoExtension.class)
class RegistrationNumberServiceTest {

	@Mock
	private RegistrationNumberSequenceRepository registrationNumberSequenceRepositoryMock;

	@InjectMocks
	private RegistrationNumberService registrationNumberService;

	@Captor
	private ArgumentCaptor<RegistrationNumberSequenceEntity> registrationNumberSequenceEntityCaptor;

	@Test
	void generateRegistrationNumber() {

		// Arrange
		final var created = OffsetDateTime.now();
		final var id = "id";
		final var modified = OffsetDateTime.now();
		final var municipalityId = "2281";
		final var sequenceNumber = 666;
		final var sequenceEntity = RegistrationNumberSequenceEntity.create()
			.withCreated(created)
			.withId(id)
			.withModified(modified)
			.withMunicipalityId(municipalityId)
			.withSequenceNumber(sequenceNumber);

		when(registrationNumberSequenceRepositoryMock.findByMunicipalityId(municipalityId)).thenReturn(Optional.of(sequenceEntity));
		when(registrationNumberSequenceRepositoryMock.save(any(RegistrationNumberSequenceEntity.class))).thenReturn(sequenceEntity);

		// Act
		final var result = registrationNumberService.generateRegistrationNumber(municipalityId);

		// Assert
		assertThat(result).isEqualTo("%s-%s-%s".formatted(now(systemDefault()).getYear(), municipalityId, sequenceNumber + 1));

		verify(registrationNumberSequenceRepositoryMock).findByMunicipalityId(municipalityId);
		verify(registrationNumberSequenceRepositoryMock).save(registrationNumberSequenceEntityCaptor.capture());

		final var capturedRegistrationNumberSequenceEntity = registrationNumberSequenceEntityCaptor.getValue();
		assertThat(capturedRegistrationNumberSequenceEntity).isNotNull();
		assertThat(capturedRegistrationNumberSequenceEntity.getMunicipalityId()).isEqualTo("2281");
		assertThat(capturedRegistrationNumberSequenceEntity.getSequenceNumber()).isEqualTo(667); // sequenceNumber incremented.
	}

	@Test
	void generateRegistrationNumberWhenNoSequenceEntityExists() {

		// Arrange
		final var created = OffsetDateTime.parse("2023-06-28T12:01:00.000+02:00");
		final var id = "id";
		final var municipalityId = "2281";
		final var sequenceNumber = 1;
		final var sequenceEntity = RegistrationNumberSequenceEntity.create()
			.withCreated(created)
			.withId(id)
			.withMunicipalityId(municipalityId)
			.withSequenceNumber(sequenceNumber);

		when(registrationNumberSequenceRepositoryMock.findByMunicipalityId(municipalityId)).thenReturn(empty());
		when(registrationNumberSequenceRepositoryMock.save(any(RegistrationNumberSequenceEntity.class))).thenReturn(sequenceEntity);

		// Act
		final var result = registrationNumberService.generateRegistrationNumber(municipalityId);

		// Assert
		assertThat(result).isEqualTo("%s-%s-%s".formatted(now(systemDefault()).getYear(), "2281", 1));

		verify(registrationNumberSequenceRepositoryMock).findByMunicipalityId(municipalityId);
		verify(registrationNumberSequenceRepositoryMock).save(registrationNumberSequenceEntityCaptor.capture());

		final var capturedRegistrationNumberSequenceEntity = registrationNumberSequenceEntityCaptor.getValue();
		assertThat(capturedRegistrationNumberSequenceEntity).isNotNull();
		assertThat(capturedRegistrationNumberSequenceEntity.getMunicipalityId()).isEqualTo("2281");
		assertThat(capturedRegistrationNumberSequenceEntity.getSequenceNumber()).isEqualTo(1); // sequenceNumber 1 due to new sequence.
	}

	@Test
	void generateRegistrationNumberWhenNewYearHasBegun() {

		// Arrange
		final var created = OffsetDateTime.parse("2022-06-28T12:01:00.000+02:00");
		final var id = "id";
		final var modified = OffsetDateTime.parse("2022-06-28T12:01:00.000+02:00");
		final var municipalityId = "2281";
		final var sequenceNumber = 666;
		final var sequenceEntity = RegistrationNumberSequenceEntity.create()
			.withCreated(created)
			.withId(id)
			.withModified(modified)
			.withMunicipalityId(municipalityId)
			.withSequenceNumber(sequenceNumber);

		when(registrationNumberSequenceRepositoryMock.findByMunicipalityId(municipalityId)).thenReturn(Optional.of(sequenceEntity));
		when(registrationNumberSequenceRepositoryMock.save(any(RegistrationNumberSequenceEntity.class))).thenReturn(sequenceEntity);

		// Act
		final var result = registrationNumberService.generateRegistrationNumber(municipalityId);

		// Assert
		assertThat(result).isEqualTo("%s-%s-%s".formatted(now(systemDefault()).getYear(), "2281", 1));

		verify(registrationNumberSequenceRepositoryMock).findByMunicipalityId(municipalityId);
		verify(registrationNumberSequenceRepositoryMock).save(registrationNumberSequenceEntityCaptor.capture());

		final var capturedRegistrationNumberSequenceEntity = registrationNumberSequenceEntityCaptor.getValue();
		assertThat(capturedRegistrationNumberSequenceEntity).isNotNull();
		assertThat(capturedRegistrationNumberSequenceEntity.getMunicipalityId()).isEqualTo("2281");
		assertThat(capturedRegistrationNumberSequenceEntity.getSequenceNumber()).isEqualTo(1); // sequenceNumber 1 due to new year.
	}
}
