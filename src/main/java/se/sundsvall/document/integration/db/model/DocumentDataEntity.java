package se.sundsvall.document.integration.db.model;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

import java.io.Serializable;
import java.util.Objects;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
		@UniqueConstraint(name = "uq_document_data_binary_id", columnNames = {"document_data_binary_id"})
	})
public class DocumentDataEntity implements Serializable {

	private static final long serialVersionUID = -7783051635903859326L;

	@Id
	@UuidGenerator
	@Column(name = "id")
	private String id;

	@Column(name = "mime_type")
	private String mimeType;

	@Column(name = "file_name")
	private String fileName;

	@Embedded
	private ConfidentialityEmbeddable confidentiality;

	@Column(name = "file_size_in_bytes")
	@ColumnDefault("0")
	private long fileSizeInBytes;

	@Column(name = "archive")
	private boolean archive = false;

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

	public ConfidentialityEmbeddable getConfidentiality() {
		return confidentiality;
	}

	public void setConfidentiality(ConfidentialityEmbeddable confidentiality) {
		this.confidentiality = confidentiality;
	}

	public DocumentDataEntity withConfidentiality(ConfidentialityEmbeddable confidentiality) {
		this.confidentiality = confidentiality;
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

	public boolean isArchive() {
		return archive;
	}

	public void setArchive(final boolean archive) {
		this.archive = archive;
	}

	public DocumentDataEntity withArchive(final boolean archive) {
		this.archive = archive;
		return this;
	}



	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("DocumentDataEntity [id=").append(id).append(", mimeType=").append(mimeType).append(", fileName=").append(fileName).append(", fileSizeInBytes=").append(fileSizeInBytes).append(
			", documentDataBinary=").append(documentDataBinary).append("]");
		return builder.toString();
	}

}
