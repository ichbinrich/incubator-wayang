package hackitbackend.dto;

public class HackItJob {
    private String id;
    private String name;
    private HackItJobStatus status;
    private long submittedOn;
    private long duration;
    private String plan;


    public HackItJob(String id, String name, HackItJobStatus status, long submittedOn, long duration, String plan) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.submittedOn = submittedOn;
        this.duration = duration;
        this.plan = plan;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public HackItJobStatus getStatus() {
        return status;
    }

    public long getSubmittedOn() {
        return submittedOn;
    }

    public long getDuration() {
        return duration;
    }

    public String getPlan() {
        return plan;
    }
}
