package com.restaurant.reservation.model;

import java.util.Arrays;
import java.util.List;

/**
 * Occasion mode. Every occasion carries the setup buffer the kitchen/floor team
 * needs before the guests are seated (cake, decoration, bill pre-printing...).
 * The buffer is applied in front of the reservation slot, so a table booked for
 * 19:00 with a 15 minute birthday buffer is held from 18:45.
 */
public enum Occasion {

    NONE("Just dining", 0),
    BIRTHDAY("Birthday / celebration", 15),
    ANNIVERSARY("Anniversary", 15),
    DATE_NIGHT("Date night", 10),
    BUSINESS("Business meal", 5),
    FAMILY("Family get-together", 5),
    OTHER("Something else", 10);

    private final String label;
    private final int setupBufferMinutes;

    Occasion(String label, int setupBufferMinutes) {
        this.label = label;
        this.setupBufferMinutes = setupBufferMinutes;
    }

    public String getLabel() {
        return label;
    }

    public int getSetupBufferMinutes() {
        return setupBufferMinutes;
    }

    public static List<Occasion> all() {
        return Arrays.asList(values());
    }

    public static Occasion parse(String value) {
        if (value == null || value.isBlank()) {
            return NONE;
        }
        return Arrays.stream(values())
                .filter(o -> o.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown occasion: " + value));
    }
}