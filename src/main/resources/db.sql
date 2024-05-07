create database if not exists bank;

use bank;

create table
    if not exists account (
        account_number varchar(20) not null primary key,
        balance decimal(9, 2) not null,
        account_type smallint not null,
        account_holder_id bigint unsigned not null
    );

create table
    if not exists person (
        id serial,
        first_name varchar(200) not null,
        middle_name varchar(200) not null,
        last_name varchar(200) not null
    );

create table
    if not exists account_transaction (
        id serial,
        operation varchar(10) not null,
        balance_before decimal(9, 2) not null,
        balance_after decimal(9, 2) not null,
        account_number varchar(20) not null
    );

alter table account add foreign key (account_holder_id) references person(id) on delete cascade;
alter table account_transaction add foreign key (account_number) references account(account_number) on delete cascade;