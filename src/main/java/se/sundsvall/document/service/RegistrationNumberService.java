package se.sundsvall.document.service;

import static java.lang.String.format;
import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import se.sundsvall.document.integration.db.RegistrationNumberSequenceRepository;
import se.sundsvall.document.integration.db.model.RegistrationNumberSequenceEntity;

/**
 * Class responsible for generating unique registration numbers.
 *
 * Registration numbers are created with the following pattern: [YYYY-MUNICIPALITY_ID-SEQUENCE]
 *
 * Example:
 * If a registrationNumber is created on date 2022-10-26 for "Sundsvall municipality" (municipalityID: 2281), for the
 * first time, the registrationNumber will be: 2022-2281-1. The next generated number will be 2022-2281-2 and so on.
 *
 * Every new year, the sequence will be reset to 1. (e.g. 2023-2281-1).
 */
@Service
@Transactional
public class RegistrationNumberService {

	private static final int SEQUENCE_START = 1;
	private static final String REGISTRATION_NUMBER_TEMPLATE = "%s-%s-%s"; // [YYYY-MUNICIPALITY_ID-SEQUENCE]

	private final RegistrationNumberSequenceRepository registrationNumberSequenceRepository;

	public RegistrationNumberService(RegistrationNumberSequenceRepository registrationNumberSequenceRepository) {
		this.registrationNumberSequenceRepository = registrationNumberSequenceRepository;
	}

	public String generateRegistrationNumber(String municipalityId) {

		final var sequenceEntity = registrationNumberSequenceRepository.findByMunicipalityId(municipalityId)
			.orElse(RegistrationNumberSequenceEntity.create()
				.withCreated(now(systemDefault()))
				.withMunicipalityId(municipalityId));

		// Reset sequence every year.
		if (getLastTouched(sequenceEntity).getYear() < getCurrentYear()) {
			return createRegistrationNumber(registrationNumberSequenceRepository
				.save(sequenceEntity.withSequenceNumber(SEQUENCE_START)));
		}

		return createRegistrationNumber(registrationNumberSequenceRepository
			.save(sequenceEntity.withSequenceNumber(sequenceEntity.getSequenceNumber() + 1)));
	}

	private int getCurrentYear() {
		return now(systemDefault()).getYear();
	}

	private OffsetDateTime getLastTouched(RegistrationNumberSequenceEntity sequenceEntity) {
		return sequenceEntity.getModified() == null ? sequenceEntity.getCreated() : sequenceEntity.getModified();
	}

	private String createRegistrationNumber(RegistrationNumberSequenceEntity sequenceEntity) {
		return format(REGISTRATION_NUMBER_TEMPLATE, getCurrentYear(), sequenceEntity.getMunicipalityId(), sequenceEntity.getSequenceNumber());
	}
}
