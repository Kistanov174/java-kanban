package model;

public enum Type {
    TASK("TASK"),
    EPIC("EPIC"),
    SUBTASK("SUBTASK");

    private final String title;
    Type(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
