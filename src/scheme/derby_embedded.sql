DROP TABLE APP.GENOMES;

CREATE TABLE APP.GENOMES
(
   ID_GENOME  INTEGER        GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
   NAME       VARCHAR(255)   NOT NULL,
   COMMENT    VARCHAR(255)   NOT NULL
);

ALTER TABLE APP.GENOMES
   ADD CONSTRAINT PK_ID_GENOME
   PRIMARY KEY (ID_GENOME);

DROP TABLE APP.CHROMOSOMES;

CREATE TABLE APP.CHROMOSOMES
(
   ID_CHROMOSOME  INTEGER        GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
   ID_GENOME      INTEGER        NOT NULL,
   NAME           VARCHAR(255)   NOT NULL,
   SEQUENCE       BLOB           NOT NULL
);

ALTER TABLE APP.CHROMOSOMES
  ADD CONSTRAINT GENOME_ID_REF FOREIGN KEY (ID_GENOME)
  REFERENCES APP.GENOMES (ID_GENOME)
  ON UPDATE RESTRICT
  ON DELETE CASCADE;


COMMIT;
