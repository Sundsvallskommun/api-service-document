package se.sundsvall.document.api.model;

import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Confidentiality model.")
public class Confidentiality {

	@Schema(description = """
		A flag that can be set to alert administrative users handling the information that there are some special privacy policies to follow for the person in question.
		If there are special privacy policies to follow for this record, this flag should be set to 'true', otherwise 'false'.
		""", example = "true")
	private boolean confidential;

	@Schema(description = "Legal citation", example = "25 kap. 1 § OSL")
	private String legalCitation;

	public static Confidentiality create() {
		return new Confidentiality();
	}

	public boolean isConfidential() {
		return confidential;
	}

	public void setConfidential(boolean confidential) {
		this.confidential = confidential;
	}

	public Confidentiality withConfidential(boolean confidential) {
		this.confidential = confidential;
		return this;
	}

	public String getLegalCitation() {
		return legalCitation;
	}

	public void setLegalCitation(String legalCitation) {
		this.legalCitation = legalCitation;
	}

	public Confidentiality withLegalCitation(String legalCitation) {
		this.legalCitation = legalCitation;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(confidential, legalCitation);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof final Confidentiality other)) { return false; }
		return (confidential == other.confidential) && Objects.equals(legalCitation, other.legalCitation);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("ConfidentialityModel [confidential=").append(confidential).append(", legalCitation=").append(legalCitation).append("]");
		return builder.toString();
	}
}
