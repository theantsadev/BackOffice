   create database projet_hotel;
   \c projet_hotel;

CREATE TABLE Hotel(
   id_hotel SERIAL,
   nom VARCHAR(50) ,
   PRIMARY KEY(id_hotel)
);

CREATE TABLE Reservation(
   id_reservation SERIAL,
   nb_passager INTEGER,
   date_heure_arrivee TIMESTAMP,
   id_client VARCHAR(50) ,
   id_hotel INTEGER NOT NULL,
   PRIMARY KEY(id_reservation),
   FOREIGN KEY(id_hotel) REFERENCES Hotel(id_hotel)
);

CREATE TABLE Vehicule(
   id SERIAL,
   reference VARCHAR(50) NOT NULL UNIQUE,
   place INTEGER NOT NULL,
   type_carburant CHAR(1) NOT NULL CHECK (type_carburant IN ('D', 'E')),
   PRIMARY KEY(id)
);

CREATE TABLE Token(
   id SERIAL,
   token VARCHAR(255) NOT NULL UNIQUE,
   date_heure_expiration TIMESTAMP NOT NULL,
   PRIMARY KEY(id)
);
