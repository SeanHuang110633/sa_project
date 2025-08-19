import java.time.LocalDate;
import java.util.ArrayList;

public class Member {
    private String id;
    private String name;
    private String email;
    private int age;
    private String gender;
    private String region;
    private ArrayList<Invitation> sentInvitations;
    private ArrayList<Invitation> receivedInvitations;
    private int dailyInvitationCount;
    private LocalDate lastInvitationDate;

    public Member(String id, String name, String email, int age, String gender, String region) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
        this.gender = gender;
        this.region = region;
        this.sentInvitations = new ArrayList<>();
        this.receivedInvitations = new ArrayList<>();
        this.dailyInvitationCount = 0;
        this.lastInvitationDate = LocalDate.now();
    }

    // add invitation to member if the member has sent invitation to someone
    public void addSentInvitations(Invitation invitation) {
        this.sentInvitations.add(invitation);
    }

    // add invitation to member if the member has received invitation
    public void addReceivedInvitations(Invitation invitation) {
        this.receivedInvitations.add(invitation);
    }

    /* handle daily invitation count
     * if the condition is renew , set the daily invitation count to 0
     * if the condition is add , add 1 to the daily invitation count
     * else print wrong condition
     * */
    public void handleDailyInvitationCount(String condition) {
        if(condition.equals("RENEW")) this.dailyInvitationCount = 0;
        else if(condition.equals("ADD")) this.dailyInvitationCount++;
        else System.out.println(" Wrong condition ");
    }

    // handle last invitation date
    public void handleLastInvitationDate(LocalDate date) {
        this.lastInvitationDate = date;
    }

    // getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getRegion() {
        return region;
    }

    public int getDailyInvitationCount() {
        return dailyInvitationCount;
    }

    public LocalDate getLastInvitationDate(){
        return lastInvitationDate;
    }


    // ------------------ just for testing ----------------------
    public void printInvitations() {
        System.out.println("[Sent Invitations] ");
        if(this.sentInvitations.isEmpty()) System.out.println("Empty");
        this.sentInvitations.forEach(invitation -> System.out.println(
                "{ - id : " + invitation.getId() +
                " - Receiver : " + invitation.getReceiver().getName()+
                " - Status : " + invitation.getStatus() +
                "  }"
        ));
        System.out.println("[Received Invitations]  ");
        if(this.receivedInvitations.isEmpty()) System.out.println("Empty");
        this.receivedInvitations.forEach(invitation -> System.out.println(
                "{ - id : " + invitation.getId() +
                " - Sender : " + invitation.getSender().getName()+
                " - Status : " + invitation.getStatus() +
                "  }"
        ));
    }

}
