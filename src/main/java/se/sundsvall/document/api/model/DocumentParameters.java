package se.sundsvall.document.api.model;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Objects;
import se.sundsvall.dept44.models.api.paging.AbstractParameterPagingAndSortingBase;

public class DocumentParameters extends AbstractParameterPagingAndSortingBase {

	@Schema(description = "Municipality identifier", example = "1234", accessMode = Schema.AccessMode.READ_ONLY)
	private String municipalityId;

	@Schema(description = "Should the search include confidential documents?", example = "true", defaultValue = "false")
	private boolean includeConfidential;

	@Schema(description = "Should the search include only the latest revision of the documents?", example = "true", defaultValue = "false")
	private boolean onlyLatestRevision;

	@Schema(description = "List of document types")
	private List<String> documentTypes;

	@ArraySchema(schema = @Schema(description = "List of metadata", implementation = MetaData.class))
	private List<MetaData> metaData;

	public static class MetaData {

		@Schema(description = "Metadata key", example = "Some key")
		private String key;

		@ArraySchema(schema = @Schema(description = "List of metadata values", implementation = String.class))
		private List<String> matchesAny;

		@ArraySchema(schema = @Schema(description = "List of metadata values", implementation = String.class))
		private List<String> matchesAll;

		public static DocumentParameters.MetaData create() {
			return new DocumentParameters.MetaData();
		}

		public DocumentParameters.MetaData withKey(final String key) {
			this.key = key;
			return this;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public DocumentParameters.MetaData withMatchesAny(final List<String> matchesAny) {
			this.matchesAny = matchesAny;
			return this;
		}

		public List<String> getMatchesAny() {
			return matchesAny;
		}

		public void setMatchesAny(List<String> matchesAny) {
			this.matchesAny = matchesAny;
		}

		public DocumentParameters.MetaData withMatchesAll(final List<String> matchesAll) {
			this.matchesAll = matchesAll;
			return this;
		}

		public List<String> getMatchesAll() {
			return matchesAll;
		}

		public void setMatchesAll(List<String> matchesAll) {
			this.matchesAll = matchesAll;
		}
	}

	public static DocumentParameters create() {
		return new DocumentParameters();
	}

	public DocumentParameters withMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
		return this;
	}

	public String getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(String municipalityId) {
		this.municipalityId = municipalityId;
	}

	public DocumentParameters withIncludeConfidential(final boolean includeConfidential) {
		this.includeConfidential = includeConfidential;
		return this;
	}

	public boolean isIncludeConfidential() {
		return includeConfidential;
	}

	public void setIncludeConfidential(boolean includeConfidential) {
		this.includeConfidential = includeConfidential;
	}

	public DocumentParameters withOnlyLatestRevision(final boolean onlyLatestRevision) {
		this.onlyLatestRevision = onlyLatestRevision;
		return this;
	}

	public boolean isOnlyLatestRevision() {
		return onlyLatestRevision;
	}

	public void setOnlyLatestRevision(boolean onlyLatestRevision) {
		this.onlyLatestRevision = onlyLatestRevision;
	}

	public DocumentParameters withDocumentTypes(final List<String> documentTypes) {
		this.documentTypes = documentTypes;
		return this;
	}

	public List<String> getDocumentTypes() {
		return documentTypes;
	}

	public void setDocumentTypes(List<String> documentTypes) {
		this.documentTypes = documentTypes;
	}

	public DocumentParameters withMetaData(final List<MetaData> metaData) {
		this.metaData = metaData;
		return this;
	}

	public List<MetaData> getMetaData() {
		return metaData;
	}

	public void setMetaData(List<MetaData> metaData) {
		this.metaData = metaData;
	}

	@Override
	public String toString() {
		return "DocumentParameters{" +
			"municipalityId='" + municipalityId + '\'' +
			", includeConfidential=" + includeConfidential +
			", onlyLatestRevision=" + onlyLatestRevision +
			", documentTypes=" + documentTypes +
			", metaData=" + metaData +
			", sortBy=" + sortBy +
			", sortDirection=" + sortDirection +
			", page=" + page +
			", limit=" + limit +
			'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		if (!super.equals(o))
			return false;
		DocumentParameters that = (DocumentParameters) o;
		return includeConfidential == that.includeConfidential && onlyLatestRevision == that.onlyLatestRevision && Objects.equals(municipalityId, that.municipalityId) && Objects.equals(documentTypes, that.documentTypes)
			&& Objects.equals(metaData, that.metaData);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), municipalityId, includeConfidential, onlyLatestRevision, documentTypes, metaData);
	}
}
