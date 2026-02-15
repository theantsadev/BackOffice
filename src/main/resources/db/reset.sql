-- Reset sequence for Reservation table to avoid duplicate key errors
-- Run this after truncating or deleting reservations

SELECT setval('reservation_id_reservation_seq', (SELECT COALESCE(MAX(id_reservation), 0) + 1 FROM Reservation));