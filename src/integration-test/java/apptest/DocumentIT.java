package apptest;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.document.Application;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

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

@WireMockAppTestSuite(files = "classpath:/DocumentIT/", classes = Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class DocumentIT extends AbstractAppTest {

	private static final String PATH_SUNDSVALL = "/2281/documents";
	private static final String PATH_ANGE = "/2282/documents";
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
			.withServicePath(PATH_SUNDSVALL)
			.withHttpMethod(POST)
			.withContentType(MULTIPART_FORM_DATA)
			.withRequest(multipartBodyBuilder.build())
			.withExpectedResponseStatus(CREATED)
			.sendRequestAndVerifyResponse()
			.getResponseHeaders().get("Location").getFirst();

		setupCall()
			.withServicePath(location.substring(location.indexOf(PATH_SUNDSVALL)))
			.withHttpMethod(GET)
			.withHeader(ACCEPT, APPLICATION_JSON_VALUE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(APPLICATION_JSON_VALUE))
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_updateDocument() {
		setupCall()
			.withServicePath(PATH_SUNDSVALL + "/2023-2281-123")
			.withHttpMethod(PATCH)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_readDocument() {
		setupCall()
			.withServicePath(PATH_SUNDSVALL + "/2023-2281-123")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test04_readDocumentConfidentialFail() {
		setupCall()
			.withServicePath(PATH_SUNDSVALL + "/2024-2281-999")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test05_readDocumentConfidentialSuccess() {
		setupCall()
			.withServicePath(PATH_SUNDSVALL + "/2024-2281-999?includeConfidential=true")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test06_readDocumentFile() throws IOException {
		setupCall()
			.withServicePath(PATH_SUNDSVALL + "/2023-2281-123/files/4f0a04af-942d-4ad2-b2d9-151887fc995c")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedBinaryResponse(RESPONSE_FILE_BINARY)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test07_readDocumentFileConfidentialFail() {
		setupCall()
			.withServicePath(PATH_SUNDSVALL + "/2024-2281-999/files/bd239ee1-27b8-43e7-bb0d-e4ba09b7220e")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test08_readDocumentFileConfidentialSuccess() throws IOException {
		setupCall()
			.withServicePath(PATH_SUNDSVALL + "/2024-2281-999/files/bd239ee1-27b8-43e7-bb0d-e4ba09b7220e?includeConfidential=true")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedBinaryResponse(RESPONSE_FILE_BINARY)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test09_search() {
		setupCall()
			.withServicePath(PATH_SUNDSVALL + "?query=value-3")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test10_searchConfidential() {
		setupCall()
			.withServicePath(PATH_SUNDSVALL + "?query=value-3&includeConfidential=true")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test11_searchWithWildCardAndText() {
		setupCall()
			.withServicePath(PATH_SUNDSVALL + "?query=*key2")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test12_searchWithWildCardAndTextConfidential() {
		setupCall()
			.withServicePath(PATH_SUNDSVALL + "?query=*key2&includeConfidential=true")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test13_searchWithWildCardOnly() {
		setupCall()
			.withServicePath(PATH_SUNDSVALL + "?query=*&sort=id,desc")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test14_searchInLatestRevisionWithWildCardOnly() {
		setupCall()
			.withServicePath(PATH_SUNDSVALL + "?query=*&onlyLatestRevision=true")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test15_searchWithWildCardOnlyConfidential() {
		setupCall()
			.withServicePath(PATH_SUNDSVALL + "?query=*&includeConfidential=true")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test16_searchInLatestRevisionWithWildCardOnlyConfidential() {
		setupCall()
			.withServicePath(PATH_SUNDSVALL + "?query=*&includeConfidential=true&onlyLatestRevision=true")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test17_deleteFile() {
		setupCall()
			.withServicePath(PATH_SUNDSVALL + "/2024-2281-139/files/abc078aa-9335-4b21-b04c-630e27ade51e")
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test18_deleteFileNotFound() {
		setupCall()
			.withServicePath(PATH_SUNDSVALL + "/2023-2281-123/files/6619a286-a6cc-4001-9f55-945734805e7d") // ID doesn't exist
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test19_addFileToDocument() throws FileNotFoundException {
		final var testFile = getFile(this.setupPaths().getTestDirectoryPath() + "image.png");
		final var multipartBodyBuilder = new MultipartBodyBuilder();
		multipartBodyBuilder.part("documentFile", new FileSystemResource(testFile)).filename(testFile.getName()).contentType(IMAGE_PNG);
		multipartBodyBuilder.part("document", fromTestFile(REQUEST_FILE));

		setupCall()
			.withServicePath(PATH_SUNDSVALL + "/2024-2281-991/files")
			.withHttpMethod(PUT)
			.withContentType(MULTIPART_FORM_DATA)
			.withRequest(multipartBodyBuilder.build())
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();

		setupCall()
			.withServicePath(PATH_SUNDSVALL + "/2024-2281-991")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test20_updateConfidentialityFlag() {
		setupCall()
			.withServicePath(PATH_SUNDSVALL + "/2023-2281-123/confidentiality")
			.withHttpMethod(PATCH)
			.withContentType(APPLICATION_JSON)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(NO_CONTENT)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test21_updateDocumentType() {
		setupCall()
			.withServicePath(PATH_ANGE + "/2024-2282-666?includeConfidential=true")
			.withHttpMethod(PATCH)
			.withRequest(REQUEST_FILE)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

}
