package ru.practicum.shareit.booking.status;

public enum State {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED,
    APPROVED;

    public static State from(String stateParam) {
        for (State value : State.values()) {
            if (value.name().equals(stateParam)) {
                return value;
            }
        }
        return null;
    }
}
