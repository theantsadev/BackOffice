package com.hotel.service.planification;

import java.sql.Timestamp;

public class TimeoutManager {

    public boolean doitAttendreDepart(int placesRestantes) {
        return placesRestantes > 0;
    }

    public Timestamp calculerDepartAvecAttente(Timestamp departInitial, int placesRestantes, long attenteMillis) {
        if (!doitAttendreDepart(placesRestantes)) {
            return departInitial;
        }

        return new Timestamp(departInitial.getTime() + attenteMillis);
    }
}
