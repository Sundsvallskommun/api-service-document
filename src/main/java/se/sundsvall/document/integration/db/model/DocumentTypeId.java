package se.sundsvall.document.integration.db.model;

import java.io.Serializable;
import java.util.Objects;

public class DocumentTypeId implements Serializable {

	private static final long serialVersionUID = 1259831019050766992L;

	private final String municipalityId;

	private final String type;

	public DocumentTypeId(String municipalityId, String type) {
		super();
		this.municipalityId = municipalityId;
		this.type = type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(municipalityId, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof final DocumentTypeId other)) {
			return false;
		}
		return Objects.equals(municipalityId, other.municipalityId) && Objects.equals(type, other.type);
	}

	@Override
	public String toString() {
		final var builder = new StringBuilder();
		builder.append("DocumentTypeId [municipalityId=").append(municipalityId).append(", type=").append(type).append("]");
		return builder.toString();
	}
}
