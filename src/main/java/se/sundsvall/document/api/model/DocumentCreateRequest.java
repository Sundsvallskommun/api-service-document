package se.sundsvall.document.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.List;
import java.util.Map;
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

	@Schema(description = "Key is filename and value represents if a specific file should be archived.", requiredMode = NOT_REQUIRED)
	private Map<String, Boolean> archiveMap;


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

	public Map<String, Boolean> getArchiveMap() {
		return archiveMap;
	}

	public void setArchiveMap(final Map<String, Boolean> archiveMap) {
		this.archiveMap = archiveMap;
	}

	public DocumentCreateRequest withArchiveMap(final Map<String, Boolean> archiveMap) {
		this.archiveMap = archiveMap;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DocumentCreateRequest that = (DocumentCreateRequest) o;
		return confidential == that.confidential && Objects.equals(municipalityId, that.municipalityId) && Objects.equals(createdBy, that.createdBy) && Objects.equals(description, that.description) && Objects.equals(metadataList, that.metadataList) && Objects.equals(archiveMap, that.archiveMap);
	}

	@Override
	public int hashCode() {
		return Objects.hash(municipalityId, createdBy, confidential, description, metadataList, archiveMap);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("DocumentCreateRequest [municipalityId=").append(municipalityId).append(", createdBy=").append(createdBy).append(", confidential=").append(confidential).append(", description=").append(description).append(", metadataList=").append(
			metadataList).append(", archiveMap=").append(archiveMap).append("]");
		return builder.toString();
	}
}
