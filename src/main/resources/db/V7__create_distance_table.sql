-- Table Distance
-- Note: La distance A→B = B→A. On n'insère qu'une seule direction (from_hotel < to_hotel par convention)
-- L'aéroport est représenté par id_hotel = 0
CREATE TABLE
   Distance (
      id_distance SERIAL,
      from_hotel INTEGER NOT NULL,
      to_hotel INTEGER NOT NULL,
      valeur DOUBLE PRECISION NOT NULL,
      PRIMARY KEY (id_distance),
      CONSTRAINT fk_from_hotel FOREIGN KEY (from_hotel) REFERENCES Hotel(id_hotel),
      CONSTRAINT fk_to_hotel FOREIGN KEY (to_hotel) REFERENCES Hotel(id_hotel),
      CONSTRAINT unique_distance UNIQUE (from_hotel, to_hotel),
      CONSTRAINT check_from_lt_to CHECK (from_hotel < to_hotel)
   );
