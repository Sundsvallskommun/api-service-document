package se.sundsvall.document.service;

public final class Constants {

	private Constants() {}

	// Templates
	public static final String TEMPLATE_CONTENT_DISPOSITION_HEADER_VALUE = "attachment; filename=\"%s\"";
	public static final String TEMPLATE_REGISTRATION_NUMBER = "%s-%s-%s"; // [YYYY-MUNICIPALITY_ID-SEQUENCE]

	// Error messages
	public static final String ERROR_DOCUMENT_BY_REGISTRATION_NUMBER_NOT_FOUND = "No document with registrationNumber: '%s' could be found!";
	public static final String ERROR_DOCUMENT_BY_REGISTRATION_NUMBER_AND_REVISION_NOT_FOUND = "No document with registrationNumber: '%s' and revision: '%s' could be found!";
	public static final String ERROR_DOCUMENT_FILE_BY_REGISTRATION_NUMBER_NOT_FOUND = "No document file for registrationNumber: '%s' could be found!";
	public static final String ERROR_DOCUMENT_FILE_BY_REGISTRATION_NUMBER_AND_REVISION_NOT_FOUND = "No document file content with registrationNumber: '%s' and revision: '%s' could be found!";
	public static final String ERROR_DOCUMENT_FILE_BY_ID_NOT_FOUND = "No document data with ID: '%s' could be found!";
	public static final String ERROR_DOCUMENT_FILE_BY_REGISTRATION_NUMBER_COULD_NOT_READ = "Could not read file content for document data with ID: '%s'!";
}
