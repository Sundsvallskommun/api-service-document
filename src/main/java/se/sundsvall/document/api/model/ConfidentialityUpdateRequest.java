package se.sundsvall.document.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "ConfidentialityUpdateRequest model.")
public class ConfidentialityUpdateRequest {

	@NotNull
	@Schema(description = """
		A flag that can be set to alert administrative users handling the information that there are some special privacy policies to follow for the person in question.
		If there are special privacy policies to follow for this record, this flag should be set to 'true', otherwise 'false'.
		Please note: This will affect all revisions, not just the latest revision.
		""", example = "false", requiredMode = REQUIRED)
	private Boolean confidential;

	@Schema(description = "Legal citation", example = "25 kap. 1 § OSL")
	private String legalCitation;

	@NotBlank
	@Schema(description = "Actor that performed this change", example = "username123", requiredMode = REQUIRED)
	private String changedBy;

	public static ConfidentialityUpdateRequest create() {
		return new ConfidentialityUpdateRequest();
	}

	public Boolean getConfidential() {
		return confidential;
	}

	public void setConfidential(Boolean confidential) {
		this.confidential = confidential;
	}

	public ConfidentialityUpdateRequest withConfidential(Boolean confidential) {
		this.confidential = confidential;
		return this;
	}

	public String getLegalCitation() {
		return legalCitation;
	}

	public void setLegalCitation(String legalCitation) {
		this.legalCitation = legalCitation;
	}

	public ConfidentialityUpdateRequest withLegalCitation(String legalCitation) {
		this.legalCitation = legalCitation;
		return this;
	}

	public String getChangedBy() {
		return changedBy;
	}

	public void setChangedBy(String changedBy) {
		this.changedBy = changedBy;
	}

	public ConfidentialityUpdateRequest withChangedBy(String changedBy) {
		this.changedBy = changedBy;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(changedBy, confidential, legalCitation);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof final ConfidentialityUpdateRequest other)) { return false; }
		return Objects.equals(changedBy, other.changedBy) && Objects.equals(confidential, other.confidential) && Objects.equals(legalCitation, other.legalCitation);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("ConfidentialityUpdateRequest [confidential=").append(confidential).append(", legalCitation=").append(legalCitation).append(", changedBy=").append(changedBy).append("]");
		return builder.toString();
	}
}
