package se.sundsvall.document.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import se.sundsvall.dept44.models.api.paging.PagingMetaData;

@Schema(description = "Paged document response model", accessMode = READ_ONLY)
public class PagedDocumentResponse {

	@ArraySchema(schema = @Schema(implementation = Document.class))
	private List<Document> documents;

	@JsonProperty("_meta")
	@Schema(implementation = PagingMetaData.class)
	private PagingMetaData metadata;

	public static PagedDocumentResponse create() {
		return new PagedDocumentResponse();
	}

	public List<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}

	public PagedDocumentResponse withDocuments(List<Document> documents) {
		this.documents = documents;
		return this;
	}

	public PagingMetaData getMetadata() {
		return metadata;
	}

	public void setMetadata(PagingMetaData metadata) {
		this.metadata = metadata;
	}

	public PagedDocumentResponse withMetaData(PagingMetaData metadata) {
		this.metadata = metadata;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(documents, metadata);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof final PagedDocumentResponse other)) { return false; }
		return Objects.equals(documents, other.documents) && Objects.equals(metadata, other.metadata);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("PagedDocumentResponse [documents=").append(documents).append(", metadata=").append(metadata).append("]");
		return builder.toString();
	}
}
