package com.restaurant.reservation.service;

import com.restaurant.reservation.exception.ApiException;

/**
 * Small helper to work with "HH:mm" reservation times as minutes from midnight,
 * so setup buffers and dining duration can be compared arithmetically.
 */
public final class TimeSlot {

    private TimeSlot() {
    }

    public static int parse(String time) {
        if (time == null || time.isBlank()) {
            throw new ApiException(400, "Time is required");
        }
        String value = time.trim().toUpperCase().replace(" ", "");
        boolean pm = value.endsWith("PM");
        boolean meridiem = pm || value.endsWith("AM");
        if (meridiem) {
            value = value.substring(0, value.length() - 2);
        }
        String[] parts = value.split(":");
        if (parts.length != 2) {
            throw new ApiException(400, "Time must look like HH:mm");
        }
        int hours;
        int minutes;
        try {
            hours = Integer.parseInt(parts[0]);
            minutes = parts[1].isEmpty() ? 0 : Integer.parseInt(parts[1]);
        } catch (NumberFormatException ex) {
            throw new ApiException(400, "Time must look like HH:mm");
        }
        if (minutes < 0 || minutes > 59) {
            throw new ApiException(400, "Minutes must be between 00 and 59");
        }
        if (meridiem) {
            if (hours < 1 || hours > 12) {
                throw new ApiException(400, "Hour must be between 1 and 12 for 12-hour times");
            }
            hours = hours % 12 + (pm ? 12 : 0);
        }
        if (hours < 0 || hours > 23) {
            throw new ApiException(400, "Hour must be between 00 and 23");
        }
        return hours * 60 + minutes;
    }

    public static String format(int minutesFromMidnight) {
        int normalized = Math.floorMod(minutesFromMidnight, 24 * 60);
        return String.format("%02d:%02d", normalized / 60, normalized % 60);
    }
}