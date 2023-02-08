package ru.practicum.shareit.booking.status;

public enum Status {
    WAITING,
    REJECTED,
    APPROVED,
    CANCELED;

    public static Status from(String stateParam) {
        for (Status value : Status.values()) {
            if (value.name().equals(stateParam)) {
                return value;
            }
        }
        return null;
    }
}
