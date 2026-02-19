CREATE TABLE
   Vehicule (
      id SERIAL,
      reference VARCHAR(50) NOT NULL UNIQUE,
      place INTEGER NOT NULL,
      type_carburant CHAR(1) NOT NULL CHECK (type_carburant IN ('D', 'E')),
      PRIMARY KEY (id)
   );
