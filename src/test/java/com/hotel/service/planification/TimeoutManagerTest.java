package com.hotel.service.planification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;

import org.junit.jupiter.api.Test;

class TimeoutManagerTest {

    @Test
    void vehiculeCompletDepartImmediat() {
        TimeoutManager manager = new TimeoutManager();
        Timestamp depart = Timestamp.valueOf("2026-04-02 10:00:00");

        Timestamp result = manager.calculerDepartAvecAttente(depart, 0, 30L * 60L * 1000L);

        assertFalse(manager.doitAttendreDepart(0));
        assertEquals(depart, result);
    }

    @Test
    void vehiculeIncompletAttendTempsConfigure() {
        TimeoutManager manager = new TimeoutManager();
        Timestamp depart = Timestamp.valueOf("2026-04-02 10:00:00");

        Timestamp result = manager.calculerDepartAvecAttente(depart, 2, 30L * 60L * 1000L);

        assertTrue(manager.doitAttendreDepart(2));
        assertEquals(Timestamp.valueOf("2026-04-02 10:30:00"), result);
    }

    @Test
    void vehiculeCompleteAvantTimeoutDepartAnticipe() {
        TimeoutManager manager = new TimeoutManager();
        Timestamp depart = Timestamp.valueOf("2026-04-02 10:00:00");

        Timestamp result = manager.calculerDepartAvecAttente(depart, 0, 30L * 60L * 1000L);

        assertEquals(Timestamp.valueOf("2026-04-02 10:00:00"), result);
    }
}
