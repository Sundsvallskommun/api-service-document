package se.sundsvall.document.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DocumentDataUpdateRequest model.")
public class DocumentDataUpdateRequest {

	@NotBlank
	@Schema(description = "Actor that created this object", example = "username123", requiredMode = REQUIRED)
	private String createdBy;

	@Schema(description = "Confidentiality")
	private Confidentiality confidentiality;

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

	public Confidentiality getConfidentiality() {
		return confidentiality;
	}

	public void setConfidentiality(Confidentiality confidentiality) {
		this.confidentiality = confidentiality;
	}

	public DocumentDataUpdateRequest withConfidentiality(Confidentiality confidentiality) {
		this.confidentiality = confidentiality;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(confidentiality, createdBy);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof final DocumentDataUpdateRequest other)) { return false; }
		return Objects.equals(confidentiality, other.confidentiality) && Objects.equals(createdBy, other.createdBy);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("DocumentDataUpdateRequest [createdBy=").append(createdBy).append(", confidentiality=").append(confidentiality).append("]");
		return builder.toString();
	}
}
