package hackitbackend.dto;

public class Input {
    private String type;
    private String definition;
    private String source;

    public Input(String type, String definition, String source) {
        this.type = type;
        this.definition = definition;
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public String getDefinition() {
        return definition;
    }

    public String getSource() {
        return source;
    }
}
