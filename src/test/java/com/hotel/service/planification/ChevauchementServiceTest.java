package com.hotel.service.planification;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.junit.jupiter.api.Test;

class ChevauchementServiceTest {

    @Test
    void retourPendantGroupeNormalRetourneTrue() throws SQLException {
        ChevauchementService service = new ChevauchementService() {
            @Override
            protected boolean existeDepartNormalDansFenetre(Timestamp borneDebut, Timestamp borneFin, Date date)
                    throws SQLException {
                return true;
            }
        };

        boolean result = service.chevaucheGroupeNormal(
                Timestamp.valueOf("2026-04-02 10:00:00"),
                Timestamp.valueOf("2026-04-02 10:00:00"),
                Date.from(Timestamp.valueOf("2026-04-02 00:00:00").toInstant()));

        assertTrue(result);
    }

    @Test
    void retourHorsGroupeNormalRetourneFalse() throws SQLException {
        ChevauchementService service = new ChevauchementService() {
            @Override
            protected boolean existeDepartNormalDansFenetre(Timestamp borneDebut, Timestamp borneFin, Date date)
                    throws SQLException {
                return false;
            }
        };

        boolean result = service.chevaucheGroupeNormal(
                Timestamp.valueOf("2026-04-02 16:00:00"),
                Timestamp.valueOf("2026-04-02 16:00:00"),
                Date.from(Timestamp.valueOf("2026-04-02 00:00:00").toInstant()));

        assertFalse(result);
    }
}
