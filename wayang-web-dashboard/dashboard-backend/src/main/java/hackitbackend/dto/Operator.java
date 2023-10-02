package hackitbackend.dto;

import java.util.List;

public class Operator {
    public enum Kind {
        UNARY, BINARY, SOURCE, SINK
    }
    private String id;
    private String name;
    private Kind kind;
    private String platform;
    private List<Input> inputs;
    private List<Output> outputs;
    private List<UDF> udfs;

    public Operator(String id, String name, Kind kind, String platform) {
        this.id = id;
        this.name = name;
        this.kind = kind;
        this.platform = platform;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Kind getKind() {
        return kind;
    }

    public String getPlatform() {
        return platform;
    }

    public List<Input> getInputs() {
        return inputs;
    }

    public List<Output> getOutputs() {
        return outputs;
    }

    public List<UDF> getUdfs() {
        return udfs;
    }

    public void addInput(Input in){
        this.inputs.add(in);
    }

    public Input getInput(int ix){
        return this.inputs.get(ix);
    }

    public void addOutput(Output out){
        this.outputs.add(out);
    }

    public Output getOutput(int ix){
        return this.outputs.get(ix);
    }

    public void addUdf(UDF udf){
        this.udfs.add(udf);
    }

    public UDF getUdf(int ix){
        return this.udfs.get(ix);
    }
}
