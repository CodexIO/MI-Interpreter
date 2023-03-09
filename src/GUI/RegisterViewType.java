package GUI;

public enum RegisterViewType {
    BINARY("Binär"),
    DECIMAL("Dezimal"),
    HEX("Hexadezimal"),
    FLOAT("Float"),
    DOUBLE("Double");

    private final String name;

    RegisterViewType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
