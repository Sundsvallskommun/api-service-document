package se.sundsvall.document.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

@Schema(description = "Document model.")
public class Document {

	@Schema(description = "ID of the document.", example = "0d64c132-3aea-11ec-8d3d-0242ac130003", accessMode = READ_ONLY)
	private String id;

	@Schema(description = "Registration number on the format [YYYY-nn].", example = "2023-1337", accessMode = READ_ONLY)
	private String registrationNumber;

	@Schema(description = "Document revision.", example = "2", accessMode = READ_ONLY)
	private int revision;

	@Schema(description = "Timestamp when document revision was created.", example = "2023-08-31T01:30:00.000+02:00", accessMode = READ_ONLY)
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private OffsetDateTime created;

	@Schema(description = "Actor that created this revision.", example = "username123", accessMode = READ_ONLY)
	private String createdBy;

	@Schema(description = "List of DocumentMetadata objects.")
	private List<@Valid DocumentMetadata> metadataList;

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

	@Override
	public int hashCode() {
		return Objects.hash(created, createdBy, id, metadataList, registrationNumber, revision);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof final Document other)) { return false; }
		return Objects.equals(created, other.created) && Objects.equals(createdBy, other.createdBy) && Objects.equals(id, other.id) && Objects.equals(metadataList, other.metadataList) && Objects.equals(registrationNumber, other.registrationNumber)
			&& (revision == other.revision);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Document [id=").append(id).append(", registrationNumber=").append(registrationNumber).append(", revision=").append(revision).append(", created=").append(created).append(", createdBy=").append(createdBy).append(", metadataList=")
			.append(metadataList).append("]");
		return builder.toString();
	}
}
