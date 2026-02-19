CREATE TABLE
   Token (
      id SERIAL,
      token VARCHAR(255) NOT NULL UNIQUE,
      date_heure_expiration TIMESTAMP NOT NULL,
      PRIMARY KEY (id)
   );