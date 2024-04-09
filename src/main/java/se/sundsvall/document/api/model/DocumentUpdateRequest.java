package se.sundsvall.document.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DocumentUpdateRequest model.")
public class DocumentUpdateRequest {

	@NotBlank
	@Schema(description = "Actor that created this revision (all modifications will create new revisions).", example = "username123", requiredMode = REQUIRED)
	private String createdBy;

	@Size(max = 8192)
	@Schema(description = "Document description", example = "A brief description of this object. Maximum 8192 characters.")
	private String description;

	@Schema(description = "List of DocumentMetadata objects.")
	private List<@Valid DocumentMetadata> metadataList;

	@Schema(description = "Key is filename and value represents if a specific file should be archived.", requiredMode = NOT_REQUIRED)
	private Map<String, Boolean> archiveMap;

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

	public Map<String, Boolean> getArchiveMap() {
		return archiveMap;
	}

	public void setArchiveMap(final Map<String, Boolean> archiveMap) {
		this.archiveMap = archiveMap;
	}

	public DocumentUpdateRequest withArchiveMap(final Map<String, Boolean> archiveMap) {
		this.archiveMap = archiveMap;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DocumentUpdateRequest that = (DocumentUpdateRequest) o;
		return Objects.equals(createdBy, that.createdBy) && Objects.equals(description, that.description) && Objects.equals(metadataList, that.metadataList) && Objects.equals(archiveMap, that.archiveMap);
	}

	@Override
	public int hashCode() {
		return Objects.hash(createdBy, description, metadataList, archiveMap);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("DocumentUpdateRequest [createdBy=").append(createdBy).append(", description=")
			.append(description).append(", metadataList=").append(metadataList).append(", archiveMap=").append(archiveMap).append("]");
		return builder.toString();
	}
}
