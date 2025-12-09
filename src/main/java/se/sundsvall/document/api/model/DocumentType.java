package se.sundsvall.document.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

@Schema(description = "DocumentType model.")
public class DocumentType {

	@NotBlank
	@Schema(description = "Identifier for the document type", examples = "EMPLOYMENT_CERTIFICATE", requiredMode = REQUIRED)
	private String type;

	@NotBlank
	@Schema(description = "Display name for the document type", examples = "Anställningsbevis", requiredMode = REQUIRED)
	private String displayName;

	public static DocumentType create() {
		return new DocumentType();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public DocumentType withType(String type) {
		setType(type);
		return this;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public DocumentType withDisplayName(String displayName) {
		setDisplayName(displayName);
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(displayName, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof final DocumentType other)) {
			return false;
		}
		return Objects.equals(displayName, other.displayName) && Objects.equals(type, other.type);
	}

	@Override
	public String toString() {
		final var builder = new StringBuilder();
		builder.append("DocumentType [type=").append(type).append(", displayName=").append(displayName).append("]");
		return builder.toString();
	}
}
