   alter table document 
       add column document_type_id varchar(255) not null after description;

    alter table if exists document 
       add constraint fk_document_document_type
       foreign key (document_type_id) 
       references document_type (id);
