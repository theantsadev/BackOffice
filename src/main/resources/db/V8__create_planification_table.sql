-- Table Planification
CREATE TABLE
   Planification (
      id_planification SERIAL,
      id_reservation INTEGER NOT NULL,
      id_vehicule INTEGER NOT NULL,
      date_heure_depart_aeroport TIMESTAMP NOT NULL,
      date_heure_retour_aeroport TIMESTAMP NOT NULL,
      PRIMARY KEY (id_planification),
      CONSTRAINT fk_reservation FOREIGN KEY (id_reservation) REFERENCES Reservation(id_reservation),
      CONSTRAINT fk_vehicule FOREIGN KEY (id_vehicule) REFERENCES Vehicule(id)
   );
