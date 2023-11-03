
    create table document (
        revision integer,
        created datetime(6),
        created_by varchar(255),
        document_data_id varchar(255),
        id varchar(255) not null,
        municipality_id varchar(255),
        registration_number varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table document_data (
        file_size_in_bytes bigint default 0,
        file_name varchar(255),
        id varchar(255) not null,
        mime_type varchar(255),
        file longblob,
        primary key (id)
    ) engine=InnoDB;

    create table document_metadata (
        document_id varchar(255) not null,
        `key` varchar(255),
        value varchar(255)
    ) engine=InnoDB;

    create table registration_number_sequence (
        sequence_number integer,
        created datetime(6),
        modified datetime(6),
        id varchar(255) not null,
        municipality_id varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create index ix_registration_number 
       on document (registration_number);

    create index ix_created_by 
       on document (created_by);

    create index ix_municipality_id 
       on document (municipality_id);

    alter table if exists document 
       add constraint uq_revision_and_registration_number unique (revision, registration_number);

    alter table if exists document 
       add constraint UK_dte6vwi6c7hh3rcta5ace6k3t unique (document_data_id);

    create index ix_key 
       on document_metadata (`key`);

    create index ix_municipality_id 
       on registration_number_sequence (municipality_id);

    alter table if exists registration_number_sequence 
       add constraint uq_municipality_id unique (municipality_id);

    alter table if exists document 
       add constraint fk_document_document_data 
       foreign key (document_data_id) 
       references document_data (id);

    alter table if exists document_metadata 
       add constraint fk_document_metadata_document 
       foreign key (document_id) 
       references document (id);
