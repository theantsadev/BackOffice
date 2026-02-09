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
