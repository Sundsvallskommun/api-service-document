package se.sundsvall.document.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

public class DocumentTypeCreateRequest {

	@NotBlank
	@Schema(description = "Identifier for the document type", example = "EMPLOYMENT_CERTIFICATE", requiredMode = REQUIRED)
	private String type;

	@NotBlank
	@Schema(description = "Display name for the document type", example = "Anställningsbevis", requiredMode = REQUIRED)
	private String displayName;

	@NotBlank
	@Schema(description = "Identifier for performing person", example = "username123", requiredMode = REQUIRED)
	private String createdBy;

	public static DocumentTypeCreateRequest create() {
		return new DocumentTypeCreateRequest();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public DocumentTypeCreateRequest withType(String type) {
		setType(type);
		return this;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public DocumentTypeCreateRequest withDisplayName(String displayName) {
		setDisplayName(displayName);
		return this;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public DocumentTypeCreateRequest withCreatedBy(String createdBy) {
		setCreatedBy(createdBy);
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(createdBy, displayName, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof final DocumentTypeCreateRequest other)) {
			return false;
		}
		return Objects.equals(createdBy, other.createdBy) && Objects.equals(displayName, other.displayName) && Objects.equals(type, other.type);
	}

	@Override
	public String toString() {
		final var builder = new StringBuilder();
		builder.append("DocumentTypeCreateRequest [type=").append(type).append(", displayName=").append(displayName).append(", createdBy=").append(createdBy).append("]");
		return builder.toString();
	}
}
