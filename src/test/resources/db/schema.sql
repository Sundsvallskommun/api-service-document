
    create table document (
        revision integer,
        created datetime(6),
        created_by varchar(255),
        document_data_id varchar(255),
        id varchar(255) not null,
        registration_number varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table document_data (
        id varchar(255) not null,
        file longblob,
        primary key (id)
    ) engine=InnoDB;

    create table document_metadata (
        document_id varchar(255) not null,
        `key` varchar(255),
        value varchar(255)
    ) engine=InnoDB;

    create index ix_registration_number 
       on document (registration_number);

    create index ix_created_by 
       on document (created_by);

    alter table if exists document 
       add constraint uq_revision_and_registration_number unique (revision, registration_number);

    alter table if exists document 
       add constraint UK_dte6vwi6c7hh3rcta5ace6k3t unique (document_data_id);

    create index ix_key 
       on document_metadata (`key`);

    alter table if exists document 
       add constraint fk_document_document_data 
       foreign key (document_data_id) 
       references document_data (id);

    alter table if exists document_metadata 
       add constraint fk_document_metadata_document 
       foreign key (document_id) 
       references document (id);
