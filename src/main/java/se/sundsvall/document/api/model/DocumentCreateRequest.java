package se.sundsvall.document.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;
import java.util.Objects;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DocumentCreateRequest model.")
public class DocumentCreateRequest {

	@ValidMunicipalityId
	@Schema(description = "Municipality ID", example = "2281", requiredMode = REQUIRED)
	private String municipalityId;

	@NotBlank
	@Schema(description = "Actor that created this revision (all modifications will create new revisions)", example = "username123", requiredMode = REQUIRED)
	private String createdBy;

	@Schema(description = "Confidentiality")
	private Confidentiality confidentiality;

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

	public Confidentiality getConfidentiality() {
		return confidentiality;
	}

	public void setConfidentiality(Confidentiality confidentiality) {
		this.confidentiality = confidentiality;
	}

	public DocumentCreateRequest withConfidentiality(Confidentiality confidentiality) {
		this.confidentiality = confidentiality;
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
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DocumentCreateRequest that = (DocumentCreateRequest) o;
		return Objects.equals(municipalityId, that.municipalityId) && Objects.equals(createdBy, that.createdBy) && Objects.equals(confidentiality, that.confidentiality) && Objects.equals(description, that.description) && Objects.equals(metadataList, that.metadataList);
	}

	@Override
	public int hashCode() {
		return Objects.hash(municipalityId, createdBy, confidentiality, description, metadataList);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("DocumentCreateRequest [municipalityId=").append(municipalityId).append(", createdBy=").append(createdBy).append(", confidentiality=").append(confidentiality).append(", description=").append(description).append(", metadataList=").append(
			metadataList).append("]");
		return builder.toString();
	}
}
