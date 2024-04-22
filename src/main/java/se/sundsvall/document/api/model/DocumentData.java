package se.sundsvall.document.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DocumentData model.", accessMode = READ_ONLY)
public class DocumentData {

	@Schema(description = "ID of the document data.", example = "082ba08f-03c7-409f-b8a6-940a1397ba38")
	private String id;

	@Schema(description = "File name.", example = "my-file.pdf")
	private String fileName;

	@Schema(description = "The mime type of the file.", example = "application/pdf")
	private String mimeType;

	@Schema(description = "File size in bytes", example = "5068")
	private long fileSizeInBytes;

	public static DocumentData create() {
		return new DocumentData();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public DocumentData withId(String id) {
		this.id = id;
		return this;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public DocumentData withFileName(String fileName) {
		this.fileName = fileName;
		return this;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public DocumentData withMimeType(String mimeType) {
		this.mimeType = mimeType;
		return this;
	}

	public long getFileSizeInBytes() {
		return fileSizeInBytes;
	}

	public void setFileSizeInBytes(long fileSizeInBytes) {
		this.fileSizeInBytes = fileSizeInBytes;
	}

	public DocumentData withFileSizeInBytes(long fileSizeInBytes) {
		this.fileSizeInBytes = fileSizeInBytes;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DocumentData that = (DocumentData) o;
		return fileSizeInBytes == that.fileSizeInBytes && Objects.equals(id, that.id) && Objects.equals(fileName, that.fileName) && Objects.equals(mimeType, that.mimeType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, fileName, mimeType, fileSizeInBytes);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("DocumentData [id=").append(id).append(", fileName=").append(fileName).append(", mimeType=").append(mimeType)
			.append(", fileSizeInBytes=").append(fileSizeInBytes).append("]");
		return builder.toString();
	}
}
