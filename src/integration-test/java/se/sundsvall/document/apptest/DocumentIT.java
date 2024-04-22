package se.sundsvall.document.apptest;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.util.ResourceUtils.getFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.document.Application;

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
		multipartBodyBuilder.part("documentFiles", new FileSystemResource(testFile)).filename(testFile.getName()).contentType(IMAGE_PNG);
		multipartBodyBuilder.part("document", fromTestFile(REQUEST_FILE));

		final var location = setupCall()
			.withServicePath(PATH)
			.withHttpMethod(POST)
			.withContentType(MULTIPART_FORM_DATA)
			.withRequest(multipartBodyBuilder.build())
			.withExpectedResponseStatus(CREATED)
			.sendRequestAndVerifyResponse()
			.getResponseHeaders().get("Location").getFirst();

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
		setupCall()
			.withServicePath(PATH + "/2023-2281-123")
			.withHttpMethod(PATCH)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_readDocument() {
		setupCall()
			.withServicePath(PATH + "/2023-2281-123")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_readDocumentConfidentialFail() {
		setupCall()
			.withServicePath(PATH + "/2024-2281-999")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_readDocumentConfidentialSuccess() {
		setupCall()
			.withServicePath(PATH + "/2024-2281-999?includeConfidential=true")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test06_readDocumentFile() throws IOException {
		setupCall()
			.withServicePath(PATH + "/2023-2281-123/files/4f0a04af-942d-4ad2-b2d9-151887fc995c")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedBinaryResponse(RESPONSE_FILE_BINARY)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test07_readDocumentFileConfidentialFail() throws IOException {
		setupCall()
			.withServicePath(PATH + "/2024-2281-999/files/bd239ee1-27b8-43e7-bb0d-e4ba09b7220e")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test08_readDocumentFileConfidentialSuccess() throws IOException {
		setupCall()
			.withServicePath(PATH + "/2024-2281-999/files/bd239ee1-27b8-43e7-bb0d-e4ba09b7220e?includeConfidential=true")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedBinaryResponse(RESPONSE_FILE_BINARY)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test09_search() {
		setupCall()
			.withServicePath(PATH + "?query=value-3")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test10_searchConfidential() {
		setupCall()
			.withServicePath(PATH + "?query=value-3&includeConfidential=true")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test11_searchWithWildCardAndText() {
		setupCall()
			.withServicePath(PATH + "?query=*key2")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test12_searchWithWildCardAndTextConfidential() {
		setupCall()
			.withServicePath(PATH + "?query=*key2&includeConfidential=true")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test13_searchWithWildCardOnly() {
		setupCall()
			.withServicePath(PATH + "?query=*&sort=revision,desc")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test14_searchWithWildCardOnlyConfidential() {
		setupCall()
			.withServicePath(PATH + "?query=*&includeConfidential=true")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test15_deleteFile() {
		setupCall()
			.withServicePath(PATH + "/2023-2281-123/files/4f0a04af-942d-4ad2-b2d9-151887fc995c")
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test16_deleteFileNotFound() {
		setupCall()
			.withServicePath(PATH + "/2023-2281-123/files/6619a286-a6cc-4001-9f55-945734805e7d") // ID doesn't exist
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test17_updateConfidentialityFlag() {
		setupCall()
			.withServicePath(PATH + "/2023-2281-123/confidentiality")
			.withHttpMethod(PATCH)
			.withContentType(APPLICATION_JSON)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test18_addFileToDocument() throws FileNotFoundException {
		final var testFile = getFile(this.setupPaths().getTestDirectoryPath() + "image.png");
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("documentFile", new FileSystemResource(testFile)).filename(testFile.getName()).contentType(IMAGE_PNG);
		multipartBodyBuilder.part("document", fromTestFile(REQUEST_FILE));

		setupCall()
			.withServicePath(PATH + "/2023-2281-123/files")
			.withHttpMethod(PUT)
			.withContentType(MULTIPART_FORM_DATA)
			.withRequest(multipartBodyBuilder.build())
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		setupCall()
			.withServicePath(PATH + "/2023-2281-123")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}
