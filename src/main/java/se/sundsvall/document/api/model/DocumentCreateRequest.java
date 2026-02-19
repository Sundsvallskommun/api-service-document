package se.sundsvall.document.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Objects;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "DocumentCreateRequest model.")
public class DocumentCreateRequest {

	@NotBlank
	@Schema(description = "Actor that created this revision (all modifications will create new revisions)", examples = "username123", requiredMode = REQUIRED)
	private String createdBy;

	@Schema(description = "Confidentiality")
	private Confidentiality confidentiality;

	@Schema(description = "Tells if the document is eligible for archiving", examples = "false")
	private boolean archive;

	@NotBlank
	@Size(max = 8192)
	@Schema(description = "Document description", examples = "A brief description of this object. Maximum 8192 characters.", requiredMode = REQUIRED)
	private String description;

	@NotEmpty
	@Schema(description = "List of DocumentMetadata objects.", requiredMode = REQUIRED)
	private List<@Valid DocumentMetadata> metadataList;

	@NotBlank
	@Schema(description = "The type of document (validated against a defined list of document types).", examples = "EMPLOYMENT_CERTIFICATE", requiredMode = REQUIRED)
	private String type;

	public static DocumentCreateRequest create() {
		return new DocumentCreateRequest();
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

	public boolean isArchive() {
		return archive;
	}

	public void setArchive(boolean archive) {
		this.archive = archive;
	}

	public DocumentCreateRequest withArchive(boolean archive) {
		this.archive = archive;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public DocumentCreateRequest withType(String type) {
		this.type = type;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(archive, confidentiality, createdBy, description, metadataList, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof final DocumentCreateRequest other)) {
			return false;
		}
		return archive == other.archive && Objects.equals(confidentiality, other.confidentiality) && Objects.equals(createdBy, other.createdBy) && Objects.equals(description, other.description) && Objects.equals(metadataList, other.metadataList)
			&& Objects.equals(type, other.type);
	}

	@Override
	public String toString() {
		final var builder = new StringBuilder();
		builder.append("DocumentCreateRequest [createdBy=").append(createdBy).append(", confidentiality=").append(confidentiality).append(", archive=").append(archive).append(", description=").append(description).append(", metadataList=").append(
			metadataList).append(", type=").append(type).append("]");
		return builder.toString();
	}
}
