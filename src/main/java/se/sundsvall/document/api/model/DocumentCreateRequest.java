package se.sundsvall.document.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;

@Schema(description = "DocumentCreateRequest model.")
public class DocumentCreateRequest {

	@ValidMunicipalityId
	@Schema(description = "Municipality ID", example = "2281", requiredMode = REQUIRED)
	private String municipalityId;

	@NotBlank
	@Schema(description = "Actor that created this revision.", example = "username123", requiredMode = REQUIRED)
	private String createdBy;

	@Schema(description = "List of DocumentMetadata objects.")
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
		return Objects.hash(createdBy, metadataList, municipalityId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof final DocumentCreateRequest other)) { return false; }
		return Objects.equals(createdBy, other.createdBy) && Objects.equals(metadataList, other.metadataList) && Objects.equals(municipalityId, other.municipalityId);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("DocumentCreateRequest [municipalityId=").append(municipalityId).append(", createdBy=").append(createdBy).append(", metadataList=").append(metadataList).append("]");
		return builder.toString();
	}
}
