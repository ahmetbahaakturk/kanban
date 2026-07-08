CREATE TABLE boards (
    public_id varchar(50) PRIMARY KEY ,
    created_date TIMESTAMP WITH TIME ZONE NOT NULL default current_timestamp
);
