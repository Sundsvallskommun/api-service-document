package se.sundsvall.document.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DocumentDataCreateRequest model.")
public class DocumentDataCreateRequest {

	@NotBlank
	@Schema(description = "Actor that created this revision", example = "username123", requiredMode = REQUIRED)
	private String createdBy;

	public static DocumentDataCreateRequest create() {
		return new DocumentDataCreateRequest();
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public DocumentDataCreateRequest withCreatedBy(String createdBy) {
		this.createdBy = createdBy;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(createdBy);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof final DocumentDataCreateRequest other)) { return false; }
		return Objects.equals(createdBy, other.createdBy);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("DocumentDataCreateRequest [createdBy=").append(createdBy).append("]");
		return builder.toString();
	}
}
