import java.time.LocalDate;

public class Invitation {
    private String id;
    private Member sender;
    private Member receiver;
    private String status;
    private LocalDate dateSent;

    public Invitation(String id, Member sender, Member receiver, String status) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.status = status;
        this.dateSent = LocalDate.now();
    }

    // getters and setters
    public String getId() {
        return id;
    }

    public Member getSender() {
        return sender;
    }

    public Member getReceiver() {
        return receiver;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getDateSent() {
        return dateSent;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
