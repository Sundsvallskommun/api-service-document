package se.sundsvall.document.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;

@Schema(description = "DocumentCreateRequest model.")
public class DocumentCreateRequest {

	@ValidMunicipalityId
	@Schema(description = "Municipality ID", example = "2281", requiredMode = REQUIRED)
	private String municipalityId;

	@NotBlank
	@Schema(description = "Actor that created this revision.", example = "username123", requiredMode = REQUIRED)
	private String createdBy;

	@Schema(description = """
		A flag that can be set to alert administrative users handling the information that there are some special privacy policies to follow for the person in question.
		If there are special privacy policies to follow for this record, this flag should be set to 'true', otherwise 'false'.
		""", example = "false", defaultValue = "false")
	private boolean confidential;

	@NotBlank
	@Size(max = 8192)
	@Schema(description = "Document description", example = "A brief description of this object. Maximum 8192 characters.", requiredMode = REQUIRED)
	private String description;

	@NotEmpty
	@Schema(description = "List of DocumentMetadata objects.", requiredMode = REQUIRED)
	private List<@Valid DocumentMetadata> metadataList;

	public static DocumentCreateRequest create() {
		return new DocumentCreateRequest();
	}

	public String getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(String municipalityId) {
		this.municipalityId = municipalityId;
	}

	public DocumentCreateRequest withMunicipalityId(String municipalityId) {
		this.municipalityId = municipalityId;
		return this;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public DocumentCreateRequest withCreatedBy(String createdBy) {
		this.createdBy = createdBy;
		return this;
	}

	public boolean isConfidential() {
		return confidential;
	}

	public void setConfidential(boolean confidential) {
		this.confidential = confidential;
	}

	public DocumentCreateRequest withConfidential(boolean confidential) {
		this.confidential = confidential;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public DocumentCreateRequest withDescription(String description) {
		this.description = description;
		return this;
	}

	public List<DocumentMetadata> getMetadataList() {
		return metadataList;
	}

	public void setMetadataList(List<DocumentMetadata> metadataList) {
		this.metadataList = metadataList;
	}

	public DocumentCreateRequest withMetadataList(List<DocumentMetadata> metadataList) {
		this.metadataList = metadataList;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(confidential, createdBy, description, metadataList, municipalityId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof final DocumentCreateRequest other)) { return false; }
		return (confidential == other.confidential) && Objects.equals(createdBy, other.createdBy) && Objects.equals(description, other.description) && Objects.equals(metadataList, other.metadataList) && Objects.equals(municipalityId, other.municipalityId);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("DocumentCreateRequest [municipalityId=").append(municipalityId).append(", createdBy=").append(createdBy).append(", confidential=").append(confidential).append(", description=").append(description).append(", metadataList=").append(
			metadataList).append("]");
		return builder.toString();
	}
}
