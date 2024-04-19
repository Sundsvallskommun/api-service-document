package se.sundsvall.document.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Document model.", accessMode = READ_ONLY)
public class Document {

	@Schema(description = "ID of the document.", example = "0d64c132-3aea-11ec-8d3d-0242ac130003")
	private String id;

	@Schema(description = "Municipality ID", example = "2281")
	private String municipalityId;

	@Schema(description = "Registration number on the format [YYYY-nnnn-nnnn].", example = "2023-2281-1337")
	private String registrationNumber;

	@Schema(description = "Document revision.", example = "2")
	private int revision;

	@Schema(description = "Confidentiality")
	private Confidentiality confidentiality;

	@Schema(description = "Document description", example = "A brief description of this object.")
	private String description;

	@Schema(description = "Timestamp when document revision was created.", example = "2023-08-31T01:30:00.000+02:00")
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private OffsetDateTime created;

	@Schema(description = "Actor that created this revision.", example = "username123")
	private String createdBy;

	@Schema(description = "Tells if the document is eligible for archiving", example = "false")
	private boolean archive;

	@Schema(description = "List of DocumentMetadata objects.")
	private List<DocumentMetadata> metadataList;

	@Schema(description = "Document data")
	private List<DocumentData> documentData;

	public static Document create() {
		return new Document();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Document withId(String id) {
		this.id = id;
		return this;
	}

	public String getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(String municipalityId) {
		this.municipalityId = municipalityId;
	}

	public Document withMunicipalityId(String municipalityId) {
		this.municipalityId = municipalityId;
		return this;
	}

	public String getRegistrationNumber() {
		return registrationNumber;
	}

	public void setRegistrationNumber(String registrationNumber) {
		this.registrationNumber = registrationNumber;
	}

	public Document withRegistrationNumber(String registrationNumber) {
		this.registrationNumber = registrationNumber;
		return this;
	}

	public int getRevision() {
		return revision;
	}

	public void setRevision(int revision) {
		this.revision = revision;
	}

	public Document withRevision(int revision) {
		this.revision = revision;
		return this;
	}

	public Confidentiality getConfidentiality() {
		return confidentiality;
	}

	public void setConfidentiality(Confidentiality confidentiality) {
		this.confidentiality = confidentiality;
	}

	public Document withConfidentiality(Confidentiality confidentiality) {
		this.confidentiality = confidentiality;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Document withDescription(String description) {
		this.description = description;
		return this;
	}

	public OffsetDateTime getCreated() {
		return created;
	}

	public void setCreated(OffsetDateTime created) {
		this.created = created;
	}

	public Document withCreated(OffsetDateTime created) {
		this.created = created;
		return this;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Document withCreatedBy(String createdBy) {
		this.createdBy = createdBy;
		return this;
	}

	public boolean isArchive() {
		return archive;
	}

	public void setArchive(boolean archive) {
		this.archive = archive;
	}

	public Document withArchive(boolean archive) {
		this.archive = archive;
		return this;
	}

	public List<DocumentMetadata> getMetadataList() {
		return metadataList;
	}

	public void setMetadataList(List<DocumentMetadata> metadataList) {
		this.metadataList = metadataList;
	}

	public Document withMetadataList(List<DocumentMetadata> metadataList) {
		this.metadataList = metadataList;
		return this;
	}

	public List<DocumentData> getDocumentData() {
		return documentData;
	}

	public void setDocumentData(List<DocumentData> documentData) {
		this.documentData = documentData;
	}

	public Document withDocumentData(List<DocumentData> documentData) {
		this.documentData = documentData;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(archive, confidentiality, created, createdBy, description, documentData, id, metadataList, municipalityId, registrationNumber, revision);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Document)) {
			return false;
		}
		Document other = (Document) obj;
		return archive == other.archive && Objects.equals(confidentiality, other.confidentiality) && Objects.equals(created, other.created) && Objects.equals(createdBy, other.createdBy) && Objects.equals(description, other.description) && Objects
			.equals(documentData, other.documentData) && Objects.equals(id, other.id) && Objects.equals(metadataList, other.metadataList) && Objects.equals(municipalityId, other.municipalityId) && Objects.equals(registrationNumber,
				other.registrationNumber) && revision == other.revision;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Document [id=").append(id).append(", municipalityId=").append(municipalityId).append(", registrationNumber=").append(registrationNumber).append(", revision=").append(revision).append(", confidentiality=").append(
			confidentiality).append(", description=").append(description).append(", created=").append(created).append(", createdBy=").append(createdBy).append(", archive=").append(archive).append(", metadataList=").append(metadataList).append(
				", documentData=").append(documentData).append("]");
		return builder.toString();
	}

}
