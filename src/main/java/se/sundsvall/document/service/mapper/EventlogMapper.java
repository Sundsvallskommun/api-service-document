package se.sundsvall.document.service.mapper;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import generated.se.sundsvall.eventlog.Event;
import generated.se.sundsvall.eventlog.EventType;
import generated.se.sundsvall.eventlog.Metadata;

public class EventlogMapper {

	private static final String OWNER = "Document";
	private static final String EXECUTED_BY_KEY = "ExecutedBy";
	private static final String REGISTRATION_NUMBER_KEY = "RegistrationNumber";

	private EventlogMapper() {}

	public static Event toEvent(EventType eventType, String registrationNumber, String message, String executedBy) {
		return new Event()
			.expires(now(systemDefault()).plusYears(10))
			.message(message)
			.owner(OWNER)
			.type(eventType)
			.metadata(toMetadatas(Map.of(
				REGISTRATION_NUMBER_KEY, registrationNumber,
				EXECUTED_BY_KEY, executedBy)));
	}

	private static List<Metadata> toMetadatas(Map<String, String> metadata) {
		return ofNullable(metadata).orElse(emptyMap()).entrySet().stream()
			.map(EventlogMapper::toMetadata)
			.toList();
	}

	private static Metadata toMetadata(Entry<String, String> entry) {
		return new Metadata().key(entry.getKey()).value(entry.getValue());
	}
}
