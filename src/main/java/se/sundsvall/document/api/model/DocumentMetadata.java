package se.sundsvall.document.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DocumentMetadata model")
public class DocumentMetadata {

	@NotBlank
	@Schema(description = "Metadata key", example = "Some key", requiredMode = REQUIRED)
	private String key;

	@NotBlank
	@Schema(description = "Metadata value", example = "Some value", requiredMode = REQUIRED)
	private String value;

	public static DocumentMetadata create() {
		return new DocumentMetadata();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public DocumentMetadata withKey(String key) {
		this.key = key;
		return this;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public DocumentMetadata withValue(String value) {
		this.value = value;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof final DocumentMetadata other)) { return false; }
		return Objects.equals(key, other.key) && Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("DocumentMetadata [key=").append(key).append(", value=").append(value).append("]");
		return builder.toString();
	}
}
