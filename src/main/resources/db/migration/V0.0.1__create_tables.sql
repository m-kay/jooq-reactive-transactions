CREATE TABLE PERSONS
(
    id   varchar(36)  not null primary key,
    name varchar(255) not null
);

CREATE TABLE ADDRESSES
(
    id varchar(36) not null primary key,
    personId varchar(36) not null,
    address varchar(255) CONSTRAINT ADDRESS_MIN_LENGTH CHECK ( CHAR_LENGTH(address) >= 10)
)