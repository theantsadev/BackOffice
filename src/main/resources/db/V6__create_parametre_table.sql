-- Table Parametre (accès par base uniquement, pas de CRUD)
CREATE TABLE
   Parametre (
      id_parametre SERIAL,
      cle VARCHAR(100) NOT NULL UNIQUE,
      valeur DOUBLE PRECISION NOT NULL,
      unite VARCHAR(50),
      PRIMARY KEY (id_parametre)
   );
