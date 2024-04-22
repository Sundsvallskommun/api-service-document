package se.sundsvall.document.integration.db.model;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;
import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import se.sundsvall.document.integration.db.model.listener.DocumentEntityListener;

@Entity
@Table(
	name = "document",
	uniqueConstraints = {
		@UniqueConstraint(name = "uq_revision_and_registration_number", columnNames = { "revision", "registration_number" })
	},
	indexes = {
		@Index(name = "ix_registration_number", columnList = "registration_number"),
		@Index(name = "ix_created_by", columnList = "created_by"),
		@Index(name = "ix_municipality_id", columnList = "municipality_id"),
		@Index(name = "ix_confidential", columnList = "confidential")
	})
@EntityListeners(DocumentEntityListener.class)
public class DocumentEntity implements Serializable {

	private static final long serialVersionUID = -4452832623957756766L;

	@Id
	@UuidGenerator
	@Column(name = "id")
	private String id;

	@Column(name = "revision", nullable = false)
	private int revision;

	@Column(name = "municipality_id")
	private String municipalityId;

	@Column(name = "registration_number", nullable = false, updatable = false)
	private String registrationNumber;

	@Column(name = "description", nullable = false, columnDefinition = "varchar(8192)")
	private String description;

	@Embedded
	private ConfidentialityEmbeddable confidentiality;

	@Column(name = "archive", nullable = false)
	private boolean archive;

	@Column(name = "created_by")
	private String createdBy;

	@Column(name = "created")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	@OneToMany(cascade = ALL, orphanRemoval = true)
	@JoinColumn(name = "document_id", referencedColumnName = "id", nullable = false, foreignKey = @ForeignKey(name = "fk_document_data_document"))
	private List<DocumentDataEntity> documentData;

	@ElementCollection(fetch = EAGER)
	@CollectionTable(name = "document_metadata",
		indexes = {
			@Index(name = "ix_key", columnList = "key")
		},
		joinColumns = @JoinColumn(
			name = "document_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(name = "fk_document_metadata_document")))
	private List<DocumentMetadataEmbeddable> metadata;

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

	public String getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(String municipalityId) {
		this.municipalityId = municipalityId;
	}

	public DocumentEntity withMunicipalityId(String municipalityId) {
		this.municipalityId = municipalityId;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public DocumentEntity withDescription(String description) {
		this.description = description;
		return this;
	}

	public ConfidentialityEmbeddable getConfidentiality() {
		return confidentiality;
	}

	public void setConfidentiality(ConfidentialityEmbeddable confidentiality) {
		this.confidentiality = confidentiality;
	}

	public DocumentEntity withConfidentiality(ConfidentialityEmbeddable confidentiality) {
		this.confidentiality = confidentiality;
		return this;
	}

	public boolean isArchive() {
		return archive;
	}

	public void setArchive(final boolean archive) {
		this.archive = archive;
	}

	public DocumentEntity withArchive(final boolean archive) {
		this.archive = archive;
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

	public List<DocumentDataEntity> getDocumentData() {
		return documentData;
	}

	public void setDocumentData(List<DocumentDataEntity> documentData) {
		this.documentData = documentData;
	}

	public DocumentEntity withDocumentData(List<DocumentDataEntity> documentData) {
		this.documentData = documentData;
		return this;
	}

	public List<DocumentMetadataEmbeddable> getMetadata() {
		return metadata;
	}

	public void setMetadata(List<DocumentMetadataEmbeddable> metadata) {
		this.metadata = metadata;
	}

	public DocumentEntity withMetadata(List<DocumentMetadataEmbeddable> metadata) {
		this.metadata = metadata;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(archive, confidentiality, created, createdBy, description, documentData, id, metadata, municipalityId, registrationNumber, revision);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DocumentEntity)) {
			return false;
		}
		DocumentEntity other = (DocumentEntity) obj;
		return archive == other.archive && Objects.equals(confidentiality, other.confidentiality) && Objects.equals(created, other.created) && Objects.equals(createdBy, other.createdBy) && Objects.equals(description, other.description) && Objects
			.equals(documentData, other.documentData) && Objects.equals(id, other.id) && Objects.equals(metadata, other.metadata) && Objects.equals(municipalityId, other.municipalityId) && Objects.equals(registrationNumber, other.registrationNumber)
			&& revision == other.revision;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DocumentEntity [id=").append(id).append(", revision=").append(revision).append(", municipalityId=").append(municipalityId).append(", registrationNumber=").append(registrationNumber).append(", description=").append(description)
			.append(", confidentiality=").append(confidentiality).append(", archive=").append(archive).append(", createdBy=").append(createdBy).append(", created=").append(created).append(", documentData=").append(documentData).append(", metadata=")
			.append(metadata).append("]");
		return builder.toString();
	}

}
