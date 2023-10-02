package hackitbackend.dto;

public class UDF {
    public enum UDFCode {
        URL,
        STRING,
        ALIAS
    }
    private UDFCode code;
    private String type;
    private int position;

    public UDF(UDFCode code, String type, int position) {
        this.code = code;
        this.type = type;
        this.position = position;
    }

    public UDFCode getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public int getPosition() {
        return position;
    }
}

