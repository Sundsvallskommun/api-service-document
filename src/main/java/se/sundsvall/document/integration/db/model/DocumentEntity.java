package se.sundsvall.document.integration.db.model;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.FetchType.LAZY;
import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import se.sundsvall.document.integration.db.model.listener.DocumentEntityListener;

@Entity
@Table(
	name = "document",
	uniqueConstraints = {
		@UniqueConstraint(name = "uq_revision_and_registration_number", columnNames = { "revision", "registration_number" }),
		@UniqueConstraint(name = "uq_document_data_id", columnNames = { "document_data_id" })
	},
	indexes = {
		@Index(name = "ix_registration_number", columnList = "registration_number"),
		@Index(name = "ix_created_by", columnList = "created_by")
	})
@EntityListeners(DocumentEntityListener.class)
public class DocumentEntity {

	@Id
	@UuidGenerator
	@Column(name = "id")
	private String id;

	@Column(name = "revision")
	private int revision;

	@Column(name = "registration_number")
	private String registrationNumber;

	@Column(name = "created_by")
	private String createdBy;

	@Column(name = "created")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	@OneToOne(fetch = LAZY, cascade = ALL, orphanRemoval = true)
	@JoinColumn(
		name = "document_data_id",
		referencedColumnName = "id",
		foreignKey = @ForeignKey(name = "fk_document_document_data"))
	private DocumentDataEntity documentData;

	@ElementCollection(fetch = EAGER)
	@CollectionTable(name = "document_metadata",
		indexes = {
			@Index(name = "ix_key", columnList = "key")
		},
		joinColumns = @JoinColumn(
			name = "document_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(name = "fk_document_metadata_document")))
	private List<DocumentMetadata> metadata;

	public static DocumentEntity create() {
		return new DocumentEntity();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public DocumentEntity withId(String id) {
		this.id = id;
		return this;
	}

	public int getRevision() {
		return revision;
	}

	public void setRevision(int revision) {
		this.revision = revision;
	}

	public DocumentEntity withRevision(int revision) {
		this.revision = revision;
		return this;
	}

	public String getRegistrationNumber() {
		return registrationNumber;
	}

	public void setRegistrationNumber(String registrationNumber) {
		this.registrationNumber = registrationNumber;
	}

	public DocumentEntity withRegistrationNumber(String registrationNumber) {
		this.registrationNumber = registrationNumber;
		return this;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public DocumentEntity withCreatedBy(String createdBy) {
		this.createdBy = createdBy;
		return this;
	}

	public OffsetDateTime getCreated() {
		return created;
	}

	public void setCreated(OffsetDateTime created) {
		this.created = created;
	}

	public DocumentEntity withCreated(OffsetDateTime created) {
		this.created = created;
		return this;
	}

	public DocumentDataEntity getDocumentData() {
		return documentData;
	}

	public void setDocumentData(DocumentDataEntity documentData) {
		this.documentData = documentData;
	}

	public DocumentEntity withDocumentData(DocumentDataEntity documentData) {
		this.documentData = documentData;
		return this;
	}

	public List<DocumentMetadata> getMetadata() {
		return metadata;
	}

	public void setMetadata(List<DocumentMetadata> metadata) {
		this.metadata = metadata;
	}

	public DocumentEntity withMetadata(List<DocumentMetadata> metadata) {
		this.metadata = metadata;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(created, createdBy, documentData, id, metadata, registrationNumber, revision);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof final DocumentEntity other)) { return false; }
		return Objects.equals(created, other.created) && Objects.equals(createdBy, other.createdBy) && Objects.equals(documentData, other.documentData) && Objects.equals(id, other.id) && Objects.equals(metadata, other.metadata) && Objects.equals(
			registrationNumber, other.registrationNumber) && (revision == other.revision);
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("DocumentEntity [id=").append(id).append(", revision=").append(revision).append(", registrationNumber=").append(registrationNumber).append(", createdBy=").append(createdBy).append(", created=").append(created).append(
			", documentData=").append(documentData).append(", metadata=").append(metadata).append("]");
		return builder.toString();
	}
}
