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

	@Schema(description = "Confidentiality")
	private Confidentiality confidentiality;

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

	public Confidentiality getConfidentiality() {
		return confidentiality;
	}

	public void setConfidentiality(Confidentiality confidentiality) {
		this.confidentiality = confidentiality;
	}

	public DocumentDataCreateRequest withConfidentiality(Confidentiality confidentiality) {
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
		if (!(obj instanceof final DocumentDataCreateRequest other)) { return false; }
		return Objects.equals(confidentiality, other.confidentiality) && Objects.equals(createdBy, other.createdBy);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("DocumentDataCreateRequest [createdBy=").append(createdBy).append(", confidentiality=").append(confidentiality).append("]");
		return builder.toString();
	}
}
