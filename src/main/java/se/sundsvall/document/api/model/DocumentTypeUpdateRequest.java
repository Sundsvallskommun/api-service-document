package se.sundsvall.document.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

public class DocumentTypeUpdateRequest {

	@Schema(description = "Display name for the document type", example = "Anställningsbevis")
	private String displayName;

	@Schema(description = "Identifier for the document type", example = "EMPLOYMENT_CERTIFICATE")
	private String type;

	@NotBlank
	@Schema(description = "Identifier for performing person", example = "username123", requiredMode = REQUIRED)
	private String updatedBy;

	public static DocumentTypeUpdateRequest create() {
		return new DocumentTypeUpdateRequest();
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public DocumentTypeUpdateRequest withDisplayName(String displayName) {
		setDisplayName(displayName);
		return this;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public DocumentTypeUpdateRequest withType(String type) {
		setType(type);
		return this;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public DocumentTypeUpdateRequest withUpdatedBy(String updatedBy) {
		setUpdatedBy(updatedBy);
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(displayName, type, updatedBy);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof final DocumentTypeUpdateRequest other)) {
			return false;
		}
		return Objects.equals(displayName, other.displayName) && Objects.equals(type, other.type) && Objects.equals(updatedBy, other.updatedBy);
	}

	@Override
	public String toString() {
		final var builder = new StringBuilder();
		builder.append("DocumentTypeUpdateRequest [displayName=").append(displayName).append(", type=").append(type).append(", updatedBy=").append(updatedBy).append("]");
		return builder.toString();
	}

}
