package se.sundsvall.document.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Objects;

@Schema(description = "DocumentUpdateRequest model.")
public class DocumentUpdateRequest {

	@NotBlank
	@Schema(description = "Actor that created this revision (all modifications will create new revisions).", example = "username123", requiredMode = REQUIRED)
	private String createdBy;

	@Size(max = 8192)
	@Schema(description = "Document description", example = "A brief description of this object. Maximum 8192 characters.")
	private String description;

	@Schema(description = "Tells if the document is eligible for archiving", example = "false")
	private Boolean archive;

	@Schema(description = "List of DocumentMetadata objects.")
	private List<@Valid DocumentMetadata> metadataList;

	@Schema(description = "The type of document (validated against a defined list of document types).", example = "EMPLOYMENT_CERTIFICATE")
	private String type;

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

	public Boolean getArchive() {
		return archive;
	}

	public void setArchive(Boolean archive) {
		this.archive = archive;
	}

	public DocumentUpdateRequest withArchive(Boolean archive) {
		this.archive = archive;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public DocumentUpdateRequest withType(String type) {
		this.type = type;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(archive, createdBy, description, metadataList, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof final DocumentUpdateRequest other)) {
			return false;
		}
		return Objects.equals(archive, other.archive) && Objects.equals(createdBy, other.createdBy) && Objects.equals(description, other.description) && Objects.equals(metadataList, other.metadataList) && Objects.equals(type, other.type);
	}

	@Override
	public String toString() {
		final var builder = new StringBuilder();
		builder.append("DocumentUpdateRequest [createdBy=").append(createdBy).append(", description=").append(description).append(", archive=").append(archive).append(", metadataList=").append(metadataList).append(", type=").append(type).append("]");
		return builder.toString();
	}

}
