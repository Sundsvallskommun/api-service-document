package se.sundsvall.document.apptest;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.util.ResourceUtils.getFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.document.Application;

@Disabled // TODO: Remove before flight
@WireMockAppTestSuite(files = "classpath:/DocumentIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class DocumentIT extends AbstractAppTest {

	private static final String PATH = "/documents";
	private static final String REQUEST_FILE = "request.json";
	private static final String RESPONSE_FILE = "response.json";
	private static final String RESPONSE_FILE_BINARY = "image.jpg";

	@Test
	void test01_createDocument() throws FileNotFoundException {

		final var testFile = getFile(this.setupPaths().getTestDirectoryPath() + "image.png");
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("documentFile", new FileSystemResource(testFile)).filename(testFile.getName()).contentType(IMAGE_PNG);
		multipartBodyBuilder.part("document", fromTestFile(REQUEST_FILE));

		final var location = setupCall()
			.withServicePath(PATH)
			.withHttpMethod(POST)
			.withContentType(MULTIPART_FORM_DATA)
			.withRequest(multipartBodyBuilder.build())
			.withExpectedResponseStatus(CREATED)
			.sendRequestAndVerifyResponse()
			.getResponseHeaders().get("Location").get(0);

		setupCall()
			.withServicePath(location.substring(location.indexOf(PATH)))
			.withHttpMethod(GET)
			.withHeader(ACCEPT, APPLICATION_JSON_VALUE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_updateDocument() throws FileNotFoundException {

		final var testFile = getFile(this.setupPaths().getTestDirectoryPath() + "image.png");
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("documentFile", new FileSystemResource(testFile)).filename(testFile.getName()).contentType(IMAGE_PNG);
		multipartBodyBuilder.part("document", fromTestFile(REQUEST_FILE));

		setupCall()
			.withServicePath(PATH + "/2023-2281-123")
			.withHttpMethod(PATCH)
			.withContentType(MULTIPART_FORM_DATA)
			.withRequest(multipartBodyBuilder.build())
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_readDocument() {
		setupCall()
			.withServicePath(PATH + "/2024-2281-999")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_readDocumentFile() throws IOException {
		setupCall()
			.withServicePath(PATH + "/2024-2281-999/file")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedBinaryResponse(RESPONSE_FILE_BINARY)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_search() {
		setupCall()
			.withServicePath(PATH + "?query=value-3")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test06_searchWithWildCardTwoMatches() {
		setupCall()
			.withServicePath(PATH + "?query=*key3")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test07_searchWithWildCardAllMatches() {
		setupCall()
			.withServicePath(PATH + "?query=*")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}
