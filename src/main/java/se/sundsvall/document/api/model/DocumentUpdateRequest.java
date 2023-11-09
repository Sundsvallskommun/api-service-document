package se.sundsvall.document.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DocumentUpdateRequest model.")
public class DocumentUpdateRequest {

	@NotBlank
	@Schema(description = "Actor that created this revision.", example = "username123", requiredMode = REQUIRED)
	private String createdBy;

	@Schema(description = """
		A flag that can be set to alert administrative users handling the information that there are some special privacy policies to follow for the person in question.
		If there are special privacy policies to follow for this record, this flag should be set to 'true', otherwise 'false'.
		""", example = "false")
	private Boolean confidential;

	@Size(max = 8192)
	@Schema(description = "Document description", example = "A short text describing the content in this object. Maximum 8192 characters.")
	private String description;

	@Schema(description = "List of DocumentMetadata objects.")
	private List<@Valid DocumentMetadata> metadataList;

	public static DocumentUpdateRequest create() {
		return new DocumentUpdateRequest();
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public DocumentUpdateRequest withCreatedBy(String createdBy) {
		this.createdBy = createdBy;
		return this;
	}

	public Boolean getConfidential() {
		return confidential;
	}

	public void setConfidential(Boolean confidential) {
		this.confidential = confidential;
	}

	public DocumentUpdateRequest withConfidential(Boolean confidential) {
		this.confidential = confidential;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public DocumentUpdateRequest withDescription(String description) {
		this.description = description;
		return this;
	}

	public List<DocumentMetadata> getMetadataList() {
		return metadataList;
	}

	public void setMetadataList(List<DocumentMetadata> metadataList) {
		this.metadataList = metadataList;
	}

	public DocumentUpdateRequest withMetadataList(List<DocumentMetadata> metadataList) {
		this.metadataList = metadataList;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(confidential, createdBy, description, metadataList);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DocumentUpdateRequest)) {
			return false;
		}
		DocumentUpdateRequest other = (DocumentUpdateRequest) obj;
		return Objects.equals(confidential, other.confidential) && Objects.equals(createdBy, other.createdBy) && Objects.equals(description, other.description) && Objects.equals(metadataList, other.metadataList);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DocumentUpdateRequest [createdBy=").append(createdBy).append(", confidential=").append(confidential).append(", description=").append(description).append(", metadataList=").append(metadataList).append("]");
		return builder.toString();
	}
}
