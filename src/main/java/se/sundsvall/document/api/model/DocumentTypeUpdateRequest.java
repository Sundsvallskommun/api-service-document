package se.sundsvall.document.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class DocumentTypeUpdateRequest {

	@NotBlank
	@Schema(description = "Display name for the document type", example = "Anställningsbevis", requiredMode = REQUIRED)
	private String displayName;

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
		return Objects.hash(displayName, updatedBy);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof final DocumentTypeUpdateRequest other)) {
			return false;
		}
		return Objects.equals(displayName, other.displayName) && Objects.equals(updatedBy, other.updatedBy);
	}

	@Override
	public String toString() {
		final var builder = new StringBuilder();
		builder.append("DocumentTypeUpdateRequest [displayName=").append(displayName).append(", updatedBy=").append(updatedBy).append("]");
		return builder.toString();
	}
}
