package hackitbackend.dto;

public class Output {
    private String type;
    private String definition;
    private String source;

    public Output(String type, String definition, String source) {
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
