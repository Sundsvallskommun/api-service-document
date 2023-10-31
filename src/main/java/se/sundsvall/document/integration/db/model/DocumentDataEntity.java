package se.sundsvall.document.integration.db.model;

import java.sql.Blob;
import java.util.Objects;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "document_data")
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
	private long fileSizeInBytes;

	@Lob
	@Column(name = "file", columnDefinition = "longblob")
	private Blob file;

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

	public Blob getFile() {
		return file;
	}

	public void setFile(Blob file) {
		this.file = file;
	}

	public DocumentDataEntity withFile(Blob file) {
		this.file = file;
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

	@Override
	public int hashCode() {
		return Objects.hash(file, fileName, fileSizeInBytes, id, mimeType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof final DocumentDataEntity other)) { return false; }
		return Objects.equals(file, other.file) && Objects.equals(fileName, other.fileName) && (fileSizeInBytes == other.fileSizeInBytes) && Objects.equals(id, other.id) && Objects.equals(mimeType, other.mimeType);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("DocumentDataEntity [id=").append(id).append(", mimeType=").append(mimeType).append(", fileName=").append(fileName).append(", fileSizeInBytes=").append(fileSizeInBytes).append(", file=").append(file).append("]");
		return builder.toString();
	}
}
