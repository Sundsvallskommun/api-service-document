package se.sundsvall.document.apptest;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.document.Application;

@WireMockAppTestSuite(files = "classpath:/DocumentRevisionIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class DocumentRevisionIT extends AbstractAppTest {

	private static final String PATH = "/documents";
	private static final String RESPONSE_FILE = "response.json";
	private static final String RESPONSE_FILE_BINARY = "image.jpg";

	@Test
	void test01_readRevisions() {
		setupCall()
			.withServicePath(PATH + "/2023-2281-123/revisions")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_readRevision() {
		setupCall()
			.withServicePath(PATH + "/2023-2281-123/revisions/2")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_readRevisionFile() throws IOException {
		setupCall()
			.withServicePath(PATH + "/2023-2281-123/revisions/3/file")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedBinaryResponse(RESPONSE_FILE_BINARY)
			.sendRequestAndVerifyResponse();
	}
}
