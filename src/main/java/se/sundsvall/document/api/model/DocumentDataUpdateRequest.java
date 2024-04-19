package se.sundsvall.document.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DocumentDataUpdateRequest model.")
public class DocumentDataUpdateRequest {

	@NotBlank
	@Schema(description = "Actor that created this revision (all modifications will create new revisions)", example = "username123", requiredMode = REQUIRED)
	private String createdBy;

	public static DocumentDataUpdateRequest create() {
		return new DocumentDataUpdateRequest();
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public DocumentDataUpdateRequest withCreatedBy(String createdBy) {
		this.createdBy = createdBy;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DocumentDataUpdateRequest that = (DocumentDataUpdateRequest) o;
		return Objects.equals(createdBy, that.createdBy);
	}

	@Override
	public int hashCode() {
		return Objects.hash(createdBy);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("DocumentDataUpdateRequest [createdBy=").append(createdBy).append("]");
		return builder.toString();
	}
}
