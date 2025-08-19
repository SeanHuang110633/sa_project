import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InvitationManager {
    // daily invitation limit
    private static final int MAX_INVITATIONS = 10;
    private ArrayList<Member> memberData;
    private ArrayList<Invitation> invitationRecords;

    public InvitationManager(ArrayList<Member> allMembers) {
        this.memberData = allMembers;
        this.invitationRecords = new ArrayList<>();
    }

    // region public methods : for external usage

    // 1. get the members that match the criteria
    public ArrayList<Member> getSpecificMemberData(HashMap<String, String> criteria) {
        // validateCriteria (private method below)
        if(!validateCriteria(criteria)) return null;

        // get parameters
        int maxAge = Integer.parseInt(criteria.get("maxAge"));
        String gender = criteria.get("gender");
        String region = criteria.get("region");

        // filter to get specific members
        Stream<Member> memberStream = this.memberData.stream();
        ArrayList<Member> specificMemberData = memberStream
                .filter(member -> member.getAge() <= maxAge)
                .filter(member -> member.getGender().equals(gender))
                .filter(member -> member.getRegion().equals(region))
                .collect(Collectors.toCollection(ArrayList::new));
        return specificMemberData;
    }

    // 2. send invitation
    public boolean sendInvitation(Member sender, Member receiver) {
        // validate parameters
        if(sender == null || receiver == null) return false;
        if(sender == receiver) return false;

        // check if renew sender's quota
        LocalDate today = LocalDate.now();
        if(sender.getLastInvitationDate().isBefore(today)) {
            sender.handleDailyInvitationCount("RENEW");
        }

        // reach daily invitation limit ,can not invite
        if(!canInvite(sender)){
            return false;
        }

        // create invitation
        Invitation invitation = createInvitation(sender, receiver);

        // handle invitation
        // 1. add invitation to record
        this.invitationRecords.add(invitation);
        // 2. add invitation to sender
        sender.addSentInvitations(invitation);
        // 3. add invitation to receiver
        receiver.addReceivedInvitations(invitation);

        // update sender status
        sender.handleDailyInvitationCount("ADD");
        sender.handleLastInvitationDate(today);

        return true;
    }
    // endregion


    // region private methods : just for internal usage

    // 1. validate criteria
    private boolean validateCriteria(HashMap<String, String> criteria) {
        int maxAge = Integer.parseInt(criteria.get("maxAge"));
        String gender = criteria.get("gender");
        String region = criteria.get("region");
        // maxAge: 0-100
        if(maxAge < 0 || maxAge > 100){
            return false;
        }
        // gender should not be empty and can only be F/M/Other
        if(gender.isEmpty() || !gender.equals("F") && !gender.equals("M") && !gender.equals("Other")){
            return false;
        }
        // region should not be empty and can only be Taipei/Kaohsing
        if(region.isEmpty() ||!region.equals("Taipei") && !region.equals("Kaohsing")){
            return false;
        }
        return true;
    }

    // 2. check if sender has quota for inviting
    private boolean canInvite(Member M) {
        return M.getDailyInvitationCount() < MAX_INVITATIONS;
    }

    // 3. create invitation
    private Invitation createInvitation(Member sender, Member receiver) {
        String id = UUID.randomUUID().toString();
        Invitation invitation = new Invitation(id, sender, receiver,"pending");
        return invitation;
    }
    // endregion
}
