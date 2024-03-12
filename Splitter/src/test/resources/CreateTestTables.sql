CREATE TABLE IF NOT EXISTS gruppe_dto
(
    id                  UUID NOT NULL DEFAULT RANDOM_UUID() PRIMARY KEY,
    name                VARCHAR(255),
    offen_fuer_personen BOOLEAN DEFAULT TRUE  NOT NULL,
    geschlossen         BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE TABLE IF NOT EXISTS mitglieder_dto
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    mitglied   VARCHAR(60) NOT NULL,
    gruppe_dto UUID REFERENCES gruppe_dto (id)
);

CREATE TABLE IF NOT EXISTS ausgabe_dto
(
    id             INT AUTO_INCREMENT PRIMARY KEY,
    gruppe_dto     UUID REFERENCES gruppe_dto (id),
    gruppe_dto_key INT,
    kreditor       VARCHAR(60),
    beschreibung   TEXT    NOT NULL,
    kosten         NUMERIC(20,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS debitoren_dto
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    debitor     VARCHAR(60) NOT NULL,
    ausgabe_dto INT REFERENCES ausgabe_dto (id)
);

CREATE TABLE IF NOT EXISTS transaktion_dto
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(60) NOT NULL,
    sendet     NUMERIC(20,2)     NOT NULL,
    an         VARCHAR(60) NOT NULL,
    gruppe_dto UUID REFERENCES gruppe_dto (id)
);