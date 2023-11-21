package se.sundsvall.document.integration.eventlog.configuration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import se.sundsvall.document.Application;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class EventlogPropertiesTest {

	@Autowired
	private EventlogProperties properties;

	@Test
	void testProperties() {
		assertThat(properties.connectTimeout()).isEqualTo(19);
		assertThat(properties.readTimeout()).isEqualTo(21);
		assertThat(properties.logKeyUuid()).isEqualTo("9043c3d3-14ff-4d44-a2a8-ffb45122f2c6");
	}
}
