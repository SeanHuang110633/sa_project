import java.util.*;

public class DatingApp {
    private final ArrayList<Member> allMembers;
    private final InvitationManager invitationManager;
    // constructor
    public DatingApp() {
        this.allMembers = fetchAllMembers();
        this.invitationManager = new InvitationManager(allMembers);
    }

    // The application starts here
    public static void main(String[] args) {
        new DatingApp().userInterface();
    }

    // Main user interface
    private void userInterface() {
        try(Scanner scanner = new Scanner(System.in)) {
            while (true) {
                // region login
                System.out.print("=> Login (please enter your name): ");
                String name = scanner.nextLine();
                Member currentMember = this.login(name);
                if (currentMember == null) {
                    System.out.println("❌ Member not found. Please try again!");
                    continue;
                }
                // endregion

                // enter the system
                System.out.println("------------ Welcome, " + currentMember.getName() + "! ------------");
                System.out.print("=> Enter I to invite (or enter E to exit): ");
                String service = scanner.nextLine();
                switch (service) {
                    case "I":
                        // Enter the criteria and get the specific members
                        ArrayList<Member> specificMembers;
                        while(true){
                            System.out.println("-------------------- Invite --------------------");

                            // ask current member to enter criteria
                            HashMap<String, String> criteria = showCriteriaInputForm(scanner);

                            // call invitationManager to get the specific members
                            specificMembers = invitationManager.getSpecificMemberData(criteria);

                            // if criteria is wrong, ask current member to try again
                            if (specificMembers == null) {
                                System.out.println("❌ Invalid criteria. Please try again!");
                                continue;
                            }

                            // the criteria is ok, but no member matches
                            if (specificMembers.isEmpty()) {
                                System.out.println("🥲 So sad ~ There are no members that suit you. Goodbye...");
                                System.exit(0);
                            }

                            break;
                        }

                        // show specific members and choose to invite
                        while(true){
                            System.out.println("---------------- Recommended members for you ----------------");

                            // Display the list of specific members
                            showSpecificMembers(specificMembers);

                            // Let the user choose a member to invite
                            Member chosenMember = chooseMember(scanner, specificMembers);
                            if(chosenMember == null) {
                                // not willing to invite
                                System.exit(0);
                            }

                            // invite the chosen member
                            boolean ok = invitationManager.sendInvitation(currentMember, chosenMember);

                            if (ok) {
                                System.out.println("\n❤️❤️❤️ Great! You have invited 【 " + chosenMember.getName() + " 】 ❤️❤️❤️");
                                System.out.println("👍 Goodbye!  Wish you a good date ~");

                                // Display outcomes
                                showOutcomes(currentMember, chosenMember);
                                System.exit(0);
                            } else {
                                System.out.println("\n🥲 So sad ~ Daily invitation limit reached. Goodbye...");
                                System.exit(0);
                            }
                        }
                    case "E":
                        System.out.println("👍 Goodbye!");
                        System.exit(0);
                    default:
                        System.out.println("❌ Something went wrong. Please try again.");
                }
            }
        }
    }

    // Prompt the user to set criteria
    private HashMap<String, String> showCriteriaInputForm(Scanner sc) {
        System.out.println("Please enter your criteria:");

        System.out.print("=> 1. Maximum age (0-100): ");
        Integer maxAge = Integer.parseInt(sc.nextLine());

        System.out.print("=> 2. Gender (F/M/Other): ");
        String gender = sc.nextLine();

        System.out.print("=> 3. Region: ");
        String region = sc.nextLine();

        HashMap <String, String> criteria = new HashMap<String, String>();
        criteria.put("maxAge", maxAge.toString());
        criteria.put("gender", gender);
        criteria.put("region", region);

        return criteria;
    }

    // Display the list of specific members
    private void showSpecificMembers(ArrayList<Member> specificMembers) {
        specificMembers.forEach(member -> {
            System.out.println(
                    "- [id]: " + member.getId() +
                            " - [name]: "  + member.getName() +
                            " - [age]: " + member.getAge() +
                            " - [gender]: " + member.getGender() +
                            " - [region]: " + member.getRegion()
            );
        });
    }

    // Let the user choose a member to invite
    private Member chooseMember(Scanner sc, ArrayList<Member> specificMembers) {
        System.out.print("=> Enter the ID to invite (or enter E to exit): ");
        String id = sc.nextLine();

        // not willing to invite
        if (id.equals("E")) {
            System.out.println("🥲 Sorry, no suitable member for you. See you...");
            return null;
        }

        // get the chosen member
        for(Member member : specificMembers) {
            if (member.getId().equals(id)) {
                return member;
            }
        }

        return null;
    }



    // ----------------------------------------------------------------
    // Simulated login
    private Member login(String name) {
        for (Member member : allMembers) {
            if (member.getName().equals(name)) {
                return member;
            }
        }
        return null;
    }
    // Sample members data
    private ArrayList<Member> fetchAllMembers() {
        System.out.println("Fetching all members...");
        List<Member> products = List.of(
                new Member("001", "Sean", "email01@gmail", 25, "M", "Taipei"),
                new Member("002", "John", "email02@gmail", 40, "M", "Taipei"),
                new Member("003", "Kate", "email03@gmail", 22, "F", "Taipei"),
                new Member("004", "Ruby", "email04@gmail", 18, "F", "Taipei"),
                new Member("005", "Sara", "email05@gmail", 25, "F", "Taipei"),
                new Member("006", "Carly", "email06@gmail", 33, "F", "Kaohsing"),
                new Member("007", "Kevin", "email07@gmail", 30, "M", "Kaohsing"),
                new Member("008", "Iris", "email08@gmail", 28, "F", "Kaohsing"),
                new Member("009", "Jenny", "email09@gmail", 21, "F", "Kaohsing"),
                new Member("010", "Issac", "email10@gmail", 43, "M", "Kaohsing")
        );
        return new ArrayList<Member>(products);
    }
    // Display outcomes
    public void showOutcomes(Member currentMember, Member chosenMember) {
        System.out.println("\n============================== Outcomes ================================\n");
        System.out.println("🤷‍♂️ Sender (" + currentMember.getName() + ")'s invitation records");
        System.out.println("[Number of invitations sent]: " + currentMember.getDailyInvitationCount());
        System.out.println("[Date of last invitation]: " + currentMember.getLastInvitationDate());
        currentMember.printInvitations();
        System.out.println();
        System.out.println("🤷‍♀️ Receiver (" + chosenMember.getName() + ")'s invitation records");
        chosenMember.printInvitations();
    }
}
