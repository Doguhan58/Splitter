create table if not exists gruppe_dto
(
  id uuid primary key not null default gen_random_uuid(),
  name varchar(255),
  offen_fuer_personen boolean default true not null,
  geschlossen boolean default false not null
);

create table if not exists mitglieder_dto
(
  id serial primary key,
  mitglied varchar(60) not null,
  gruppe_dto uuid references gruppe_dto (id)
);

create table if not exists ausgabe_dto
(
  id serial primary key,
  gruppe_dto uuid references gruppe_dto (id),
  gruppe_dto_key integer,
  kreditor varchar(60),
  beschreibung text not null,
  kosten numeric not null
);

create table if not exists debitoren_dto
(
    id serial primary key,
    debitor varchar(60) not null,
    ausgabe_dto integer references ausgabe_dto (id)
);

create table if not exists transaktion_dto
(
    id serial primary key,
    name varchar(60) not null,
    sendet numeric not null,
    an varchar(60) not null,
    gruppe_dto uuid references gruppe_dto (id)
);

