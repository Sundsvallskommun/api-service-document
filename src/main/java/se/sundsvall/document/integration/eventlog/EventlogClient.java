package se.sundsvall.document.integration.eventlog;

import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.document.integration.eventlog.configuration.EventlogConfiguration.CLIENT_ID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import generated.se.sundsvall.eventlog.Event;
import se.sundsvall.document.integration.eventlog.configuration.EventlogConfiguration;

@FeignClient(name = CLIENT_ID, url = "${integration.eventlog.url}", configuration = EventlogConfiguration.class)
public interface EventlogClient {

	/**
	 * Create a log event under logKey.
	 *
	 * @param logKey containing UUID to create event for
	 * @param event  the event to create
	 */
	@PostMapping(path = "/{logKey}", consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	ResponseEntity<Void> createEvent(@PathVariable("logKey") String logKey, @RequestBody Event event);
}
