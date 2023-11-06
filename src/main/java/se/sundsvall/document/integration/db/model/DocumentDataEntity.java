package se.sundsvall.document.integration.db.model;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

import java.util.Objects;
import java.util.Optional;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
	name = "document_data",
	uniqueConstraints = {
		@UniqueConstraint(name = "uq_document_data_binary_id", columnNames = { "document_data_binary_id" }),
	})
public class DocumentDataEntity {

	@Id
	@UuidGenerator
	@Column(name = "id")
	private String id;

	@Column(name = "mime_type")
	private String mimeType;

	@Column(name = "file_name")
	private String fileName;

	@Column(name = "file_size_in_bytes")
	@ColumnDefault("0")
	private long fileSizeInBytes;

	@OneToOne(fetch = LAZY, cascade = ALL, orphanRemoval = true)
	@JoinColumn(
		name = "document_data_binary_id",
		referencedColumnName = "id",
		foreignKey = @ForeignKey(name = "fk_document_data_document_data_binary"))
	private DocumentDataBinaryEntity documentDataBinary;

	public static DocumentDataEntity create() {
		return new DocumentDataEntity();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public DocumentDataEntity withId(String id) {
		this.id = id;
		return this;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public DocumentDataEntity withMimeType(String mimeType) {
		this.mimeType = mimeType;
		return this;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public DocumentDataEntity withFileName(String fileName) {
		this.fileName = fileName;
		return this;
	}

	public long getFileSizeInBytes() {
		return fileSizeInBytes;
	}

	public void setFileSizeInBytes(long fileSizeInBytes) {
		this.fileSizeInBytes = fileSizeInBytes;
	}

	public DocumentDataEntity withFileSizeInBytes(long fileSizeInBytes) {
		this.fileSizeInBytes = fileSizeInBytes;
		return this;
	}

	public DocumentDataBinaryEntity getDocumentDataBinary() {
		return documentDataBinary;
	}

	public void setDocumentDataBinary(DocumentDataBinaryEntity documentDataBinary) {
		this.documentDataBinary = documentDataBinary;
	}

	public DocumentDataEntity withDocumentDataBinary(DocumentDataBinaryEntity documentDataBinary) {
		this.documentDataBinary = documentDataBinary;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(documentDataBinary, fileName, fileSizeInBytes, id, mimeType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof final DocumentDataEntity other)) { return false; }
		return Objects.equals(documentDataBinary, other.documentDataBinary) && Objects.equals(fileName, other.fileName) && (fileSizeInBytes == other.fileSizeInBytes) && Objects.equals(id, other.id) && Objects.equals(mimeType, other.mimeType);
	}

	@Override
	public String toString() {
		final var documentDataBinaryId = Optional.ofNullable(documentDataBinary).map(DocumentDataBinaryEntity::getId).orElse(null);
		final StringBuilder builder = new StringBuilder();
		builder.append("DocumentDataEntity [id=").append(id).append(", mimeType=").append(mimeType).append(", fileName=").append(fileName).append(", fileSizeInBytes=").append(fileSizeInBytes).append(", documentDataBinary=").append(documentDataBinaryId)
			.append("]");
		return builder.toString();
	}
}
