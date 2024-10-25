    create table document_type (
        created datetime(6),
        last_updated datetime(6),
        created_by varchar(255),
        display_name varchar(255) not null,
        last_updated_by varchar(255),
        municipality_id varchar(255) not null,
        `type` varchar(255) not null,
        primary key (municipality_id, `type`)
    ) engine=InnoDB;

    create index ix_municipality_id_type 
       on document_type (municipality_id, `type`);

    create index ix_municipality_id 
       on document_type (municipality_id);
