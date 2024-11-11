package apptest;

import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.document.Application;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@WireMockAppTestSuite(files = "classpath:/DocumentSearchIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
@DirtiesContext
class DocumentSearchIT extends AbstractAppTest {

	private static final String PATH = "/2281/documents/filter";
	private static final String RESPONSE_FILE = "response.json";

	/**
	 * # includeConfidential - false (default)
	 * # onlyLatestRevision - false (default)
	 * # limit - 3
	 */
	@Test
	void test01_searchByParameters() {
		setupCall()
			.withServicePath(PATH + "?limit=3")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * #includeConfidential - true
	 * # onlyLatestRevision - false (default)
	 * # limit - 3
	 */
	@Test
	void test02_searchByParameters() {
		setupCall()
			.withServicePath(PATH + "?includeConfidential=true&limit=3")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * # includeConfidential - false (default)
	 * # onlyLatestRevision - true
	 * # limit - 3
	 */
	@Test
	void test03_searchByParameters() {
		setupCall()
			.withServicePath(PATH + "?onlyLatestRevision=true&limit=3")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * # includeConfidential - true
	 * # onlyLatestRevision - true
	 * # limit - 3
	 */
	@Test
	void test04_searchByParameters() {
		setupCall()
			.withServicePath(PATH + "?includeConfidential=true&onlyLatestRevision=true&limit=3")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * # includeConfidential - true
	 * # onlyLatestRevision - true
	 * # documentTypes - EMPLOYEE_CERTIFICATE
	 * # limit - 3
	 */
	@Test
	void test05_searchByParameters() {
		setupCall()
			.withServicePath(PATH + "?includeConfidential=true&onlyLatestRevision=true&documentTypes=EMPLOYEE_CERTIFICATE&limit=3")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * # includeConfidential - true
	 * # onlyLatestRevision - true
	 * # documentTypes - EMPLOYEE_CERTIFICATE,HOLIDAY_EXCHANGE
	 * # limit - 3
	 */
	@Test
	void test06_searchByParameters() {
		setupCall()
			.withServicePath(PATH + "?includeConfidential=true&onlyLatestRevision=true&documentTypes=EMPLOYEE_CERTIFICATE,HOLIDAY_EXCHANGE&limit=3")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * # includeConfidential - true
	 * # onlyLatestRevision - true
	 * # metaData { key = null, matchesAny = Company B }
	 * # limit - 3
	 */
	@Test
	void test07_searchByParameters() {
		setupCall()
			.withServicePath(PATH + "?includeConfidential=true&onlyLatestRevision=true&metaData[0].matchesAny=Company B&limit=3")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * # includeConfidential - true
	 * # onlyLatestRevision - true
	 * # metaData { key = null, matchesAny = Company B,Company C }
	 * # limit - 3
	 */
	@Test
	void test08_searchByParameters() {
		setupCall()
			.withServicePath(PATH + "?includeConfidential=true&onlyLatestRevision=true&metaData[0].matchesAny=Company B,Company C&limit=3")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * # includeConfidential - true
	 * # onlyLatestRevision - true
	 * # metaData { key = null, matchesAll = Company B,Manager }
	 * # limit - 3
	 */
	@Test
	void test09_searchByParameters() {
		setupCall()
			.withServicePath(PATH + "?includeConfidential=true&onlyLatestRevision=true&metaData[0].matchesAll=Startup Y,Analyst&limit=3")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * # includeConfidential - true
	 * # onlyLatestRevision - true
	 * # metaData { key = EMPLOYEE_UNIT }
	 * # limit - 3
	 */
	@Test
	void test10_searchByParameters() {
		setupCall()
			.withServicePath(PATH + "?includeConfidential=true&onlyLatestRevision=true&metaData[0].key=EMPLOYEE_UNIT&limit=3")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * # includeConfidential - true
	 * # onlyLatestRevision - true
	 * # metaData1 { key = EMPLOYEE_UNIT }
	 * # metaData2 { key = EMPLOYEE_TYPE }
	 * # limit - 3
	 */
	@Test
	void test11_searchByParameters() {
		setupCall()
			.withServicePath(PATH + "?includeConfidential=true&onlyLatestRevision=true&metaData[0].key=EMPLOYEE_UNIT&metaData[1].key=EMPLOYEE_TYPE&limit=3")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * # includeConfidential - true
	 * # onlyLatestRevision - true
	 * # metaData1 { key = EMPLOYEE_UNIT, matchesAny = Company B}
	 * # limit - 3
	 */
	@Test
	void test12_searchByParameters() {
		setupCall()
			.withServicePath(PATH + "?includeConfidential=true&onlyLatestRevision=true&metaData[0].key=EMPLOYEE_UNIT&metaData[0].matchesAny=Company B&limit=3")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * # includeConfidential - true
	 * # onlyLatestRevision - true
	 * # metaData1 { key = EMPLOYEE_UNIT, matchesAny = Company B}
	 * # metaData2 { key = EMPLOYEE_TYPE, matchesAny = Developer}
	 * # limit - 3
	 */
	@Test
	void test13_searchByParameters() {
		setupCall()
			.withServicePath(PATH + "?includeConfidential=true&onlyLatestRevision=true&metaData[0].key=EMPLOYEE_UNIT&metaData[0].matchesAny=Company B&metaData[1].key=EMPLOYEE_TYPE&metaData[2].matchesAny=Developer&limit=3")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

}

