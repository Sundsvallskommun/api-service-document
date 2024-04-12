package se.sundsvall.document.api.model;

import java.util.List;
import java.util.Objects;

import org.springframework.web.multipart.MultipartFile;

import se.sundsvall.document.api.validation.NoDuplicateFileNames;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DocumentFiles model")
public class DocumentFiles {

	@NoDuplicateFileNames
	@Schema(description = "List of files")
	private List<MultipartFile> files;

	public static DocumentFiles create() {
		return new DocumentFiles();
	}

	public List<MultipartFile> getFiles() {
		return files;
	}

	public void setFiles(final List<MultipartFile> files) {
		this.files = files;
	}

	public DocumentFiles withFiles(final List<MultipartFile> files) {
		this.files = files;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DocumentFiles that = (DocumentFiles) o;
		return Objects.equals(files, that.files);
	}

	@Override
	public int hashCode() {
		return Objects.hash(files);
	}

	@Override
	public String toString() {
		return "DocumentFiles{" +
			"files=" + files +
			'}';
	}

}
