package se.sundsvall.document.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DocumentUpdateRequest model.")
public class DocumentUpdateRequest {

	@Schema(description = "Actor that created this revision.", example = "username123", requiredMode = REQUIRED)
	@NotBlank
	private String createdBy;

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
		return Objects.hash(createdBy, metadataList);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof final DocumentUpdateRequest other)) { return false; }
		return Objects.equals(createdBy, other.createdBy) && Objects.equals(metadataList, other.metadataList);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("DocumentUpdateRequest [createdBy=").append(createdBy).append(", metadataList=").append(metadataList).append("]");
		return builder.toString();
	}
}
