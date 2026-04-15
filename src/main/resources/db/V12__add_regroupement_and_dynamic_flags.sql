CREATE TABLE IF NOT EXISTS Regroupement (
    id SERIAL PRIMARY KEY,
    date_debut TIMESTAMP NOT NULL,
    date_fin TIMESTAMP NOT NULL,
    type VARCHAR(10) NOT NULL,
    date_trigger TIMESTAMP,
    id_vehicule_trigger INTEGER
);

ALTER TABLE Planification
ADD COLUMN IF NOT EXISTS is_dynamique BOOLEAN DEFAULT FALSE;

ALTER TABLE Planification
ADD COLUMN IF NOT EXISTS en_attente BOOLEAN DEFAULT FALSE;

ALTER TABLE Planification
ADD COLUMN IF NOT EXISTS id_regroupement INTEGER;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_planification_regroupement'
    ) THEN
        ALTER TABLE Planification
        ADD CONSTRAINT fk_planification_regroupement
        FOREIGN KEY (id_regroupement) REFERENCES Regroupement(id);
    END IF;
END $$;