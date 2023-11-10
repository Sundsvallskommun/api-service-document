-- Add new columns to tables for support of new attributes and list of document_data
alter table document
	add confidential bit not null default false after revision,
	add description varchar(8192) not null default '' after created_by;
    
alter table document_data
	add `document_id` varchar(255) not null default '' after document_data_binary_id;

-- Convert existing document_data rows
update document_data, document set document_data.`document_id` = document.id
	where document_data.id = document.document_data_id;
	
-- Drop one_to_one releationship and default values
alter table document
	drop foreign key fk_document_document_data,
	drop key UK_dte6vwi6c7hh3rcta5ace6k3t,
	drop column document_data_id,
	alter column confidential drop default,
	alter column description drop default;

alter table document_data
	alter column `document_id` drop default;

-- Add indexes and unique constraints for new attributes
create index ix_confidential 
	on document (confidential);

create index ix_description 
	on document (description);

create index ix_document_id 
	on document_data (`document_id`);

alter table if exists document_data 
	add constraint fk_document_data_document 
	foreign key (`document_id`) 
	references document (id);
