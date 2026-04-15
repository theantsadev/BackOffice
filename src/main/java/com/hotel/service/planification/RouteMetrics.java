package com.hotel.service.planification;

public class RouteMetrics {
    private final double totalDistanceKm;
    private final long totalDurationMillis;

    public RouteMetrics(double totalDistanceKm, long totalDurationMillis) {
        this.totalDistanceKm = totalDistanceKm;
        this.totalDurationMillis = totalDurationMillis;
    }

    public double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public long getTotalDurationMillis() {
        return totalDurationMillis;
    }
}
