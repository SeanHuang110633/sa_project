import javax.swing.*;
// 在容器四周加「留白」邊界，讓畫面不會太擁擠
import javax.swing.border.EmptyBorder;
// JTable 的資料模型（行列資料、是否可編輯等）
import javax.swing.table.DefaultTableModel;
// Abstract Window Toolkit 底層 GUI 相關類別，Swing 版面配置器大多在這包。
import java.awt.*;
import java.util.*;
import java.util.List;

public class DatingApp {

    // instance variables
    private final ArrayList<Member> allMembers;
    private final InvitationManager invitationManager;
    private Member currentMember;
    private ArrayList<Member> specificMembers = new ArrayList<>();

    // region =>  GUI fields
    // main frame
    private JFrame frame;
    // criteria input items
    private JTextField maxAge;
    private JComboBox<String> gender;
    private JComboBox<String> region;
    // handle recommended member table
    private JTable table;
    private DefaultTableModel tableModel;
    // buttons
    private JButton btnSearch;
    private JButton btnInvite;
    private JButton btnCancelExit;
    // endregion

    // constructor
    public DatingApp() {
        this.allMembers = fetchAllMembers();
        this.invitationManager = new InvitationManager(allMembers);
    }

    public static void main(String[] args) {
        try {
            // Set the Nimbus look and feel
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        // Create and display the form
        SwingUtilities.invokeLater(() -> {
            DatingApp app = new DatingApp();

            // pop up login dialog first
            boolean ok = app.showLoginDialog();
            if (!ok) {
                System.exit(0);
            }

            // Successfully login, then pop up pre-invite dialog
            boolean chosenService = app.showServiceDialog();
            if (!chosenService) {
                System.exit(0);
            }

            // currentMember chooses Invite then enter the launchUI for inviting flow
            app.launchUI();
        });
    }
    
    // region => GUI
    // =========================== GUI ==========================
    // 1. main UI
    private void launchUI() {
        frame = new JFrame("Dating App");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(920, 620);
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        setCriteriaEnabled(true);

        frame.setContentPane(root); // set root as the frame's main pane
        frame.setVisible(true);
    }

    // 2. header just for title
    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Dating App", SwingConstants.LEFT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
        JPanel text = new JPanel();
        text.add(title);
        header.add(text, BorderLayout.WEST);

        return header;
    }

    // 3. center for criteria and display the specific members
    private JComponent buildCenter() {
        JPanel center = new JPanel();
        center.setLayout(new BorderLayout(0, 10));
        center.setBorder(new EmptyBorder(12, 0, 12, 0));

        // Criteria panel
        JPanel criteria = new JPanel();
        criteria.setLayout(new GridBagLayout());
        criteria.setBorder(new TitledBorderEx("Search Criteria"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        maxAge = new JTextField();
        gender = new JComboBox<>(new String[]{"F", "M", "Other"});
        region = new JComboBox<>(new String[]{"Taipei", "Kaohsing"});
        btnSearch = new JButton("Search");

        int col = 0;
        // 第一列、第一欄（gridy=0, gridx=0）放Maximum age (0-100)標籤
        gc.gridy = 0;
        gc.gridx = col++;
        criteria.add(new JLabel("Maximum age (0-100):"), gc);
        // 第一列、第二欄（gridy=0, gridx=1）放 tfMaxAge。
        gc.gridx = col;
        gc.weightx = 1;
        criteria.add(maxAge, gc);
        gc.weightx = 0;

        // 第二列、第一欄（gridy=1, gridx=0）放 Gender標籤
        gc.gridy = 1;
        col = 0;
        gc.gridx = col++;
        criteria.add(new JLabel("Gender:"), gc);
        // 第二列、第二欄（gridy=1, gridx=1）放 tfMaxAge。
        gc.gridx = col;
        criteria.add(gender, gc);

        gc.gridy = 2;
        col = 0;
        gc.gridx = col++;
        criteria.add(new JLabel("Region:"), gc);
        gc.gridx = col;
        criteria.add(region, gc);

        // 搜尋按鈕放在第 0 列第 3 欄
        gc.gridy = 0;
        gc.gridx = 3;
        gc.gridheight = 3; // 縱向跨 3 列（0,1,2）
        gc.fill = GridBagConstraints.BOTH; // 在儲存格中橫向與直向都填滿
        criteria.add(btnSearch, gc);

        /** 搜尋結果 Table
         * 第一個參數是欄名陣列；第二個參數 0 是初始列數。
         * 這裡覆寫 isCellEditable(...) 回傳 false：表示整張表不可編輯，避免使用者直接改表格內容。
         */
        tableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Age", "Gender", "Region"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        // 建立 JTable，資料來源綁到 tableModel。表格內容之後透過 tableModel.addRow() 來更新 (在下面的refreshTable方法)。
        table = new JTable(tableModel);
        table.setRowHeight(22);
        table.setFillsViewportHeight(true);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new TitledBorderEx("Recommended Members"));

        center.add(criteria, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);

        // handle search button
        btnSearch.addActionListener(e -> doSearch());

        return center;
    }

    // 4. footer for handling invite and exit
    private JComponent buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());

        JPanel actions = new JPanel();
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));
        btnInvite = new JButton("Invite Selected");
        btnCancelExit = new JButton("Cancel and Exit");
        actions.add(btnInvite);
        actions.add(Box.createHorizontalStrut(10));
        actions.add(btnCancelExit);

        footer.add(actions, BorderLayout.EAST);

        // handle invite button
        btnInvite.addActionListener(e -> doInvite());
        btnCancelExit.addActionListener(e -> System.exit(0));

        btnInvite.setEnabled(false); // 讓按鈕一開始是無法點擊的（灰色狀態），避免用戶在還沒搜尋或選擇之前就亂點。
        btnCancelExit.setEnabled(false); // 同理
        return footer;
    }

    // control the enabled status of criteria inputs and invite buttons
    private void setCriteriaEnabled(boolean enabled) {
        maxAge.setEnabled(enabled);
        gender.setEnabled(enabled);
        region.setEnabled(enabled);
        btnSearch.setEnabled(enabled);
        // 只有在 enabled == true 且 tableModel 裡有至少一筆資料時，「邀請」按鈕才可用。
        btnInvite.setEnabled(enabled && tableModel.getRowCount() > 0);
    }
    // endregion

    // region => Actions
    // ==================== Actions ==================

    private void doSearch() {

        String ageStr = maxAge.getText().trim();

        // get criteria
        HashMap<String, String> criteria = new HashMap<>();
        criteria.put("maxAge", ageStr);
        criteria.put("gender", Objects.toString(gender.getSelectedItem(), ""));
        criteria.put("region", Objects.toString(region.getSelectedItem(), ""));

        // git specific members
        ArrayList<Member> list = invitationManager.getSpecificMemberData(criteria);

        if (list == null) {
            error("Invalid criteria. Please try again!");
            return;
        }
        if (list.isEmpty()) {
            info("So sad ~ There are no members that suit you. Goodbye...");
            System.exit(0);
            return;
        }

        specificMembers = list;
        refreshTable(specificMembers);
        btnInvite.setEnabled(true);
        btnCancelExit.setEnabled(true);
    }

    private void doInvite() {

        // get the selected member id
        int row = table.getSelectedRow();
        String id = String.valueOf(tableModel.getValueAt(row, 0));

        // filter to get the chosen member
        Member chosen = specificMembers.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElse(null);

        // send invitation
        boolean ok = invitationManager.sendInvitation(currentMember, chosen);

        // check if successfully invited
        if (ok) {
            info("\n❤️❤️❤️ Great! You have invited 【 " + chosen.getName() + " 】 ❤️❤️❤️"
                    + "\nGoodbye! Wish you a good date ~");
            showOutcomesDialog(currentMember, chosen);
            System.exit(0);
        } else {
            info("\nSo sad ~ Daily invitation limit reached. Goodbye...");
            System.exit(0);
        }
    }

    // endregion

    // region => functions which irrelevant to the main use case
    // 1. fetch mock data
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

    // 2. simulate login
    private boolean showLoginDialog() {
        JDialog dialog = new JDialog((Frame) null, "Login", true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setSize(390, 130);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel();
        form.setBorder(new EmptyBorder(12, 12, 0, 12));
        form.setLayout(new BoxLayout(form, BoxLayout.X_AXIS));
        form.add(new JLabel("Login name: "));
        JTextField tf = new JTextField();
        tf.setFont(new Font("SansSerif", Font.PLAIN, 12));
        form.add(Box.createHorizontalStrut(8));
        form.add(tf);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("Login");
        JButton cancel = new JButton("Cancel");
        actions.add(ok);
        actions.add(cancel);

        final boolean[] success = {false};

        ok.addActionListener(e -> {
            String name = tf.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter your name.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Member found = null;
            for(Member member : allMembers) {
                if (member.getName().equals(name)) {
                    found = member;
                    break;
                }
            }

            if (found == null) {
                JOptionPane.showMessageDialog(dialog, "❌ Member not found. Please try again!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            currentMember = found; // set currentMember
            success[0] = true;
            dialog.dispose();
        });

        cancel.addActionListener(e -> {
            success[0] = false;
            dialog.dispose();
        });

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.setVisible(true);
        return success[0];
    }

    // 3. choose service：Invite / Exit
    private boolean showServiceDialog() {
        JDialog dialog = new JDialog((Frame) null, "Choose Service", true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setSize(360, 160);
        dialog.setLocationRelativeTo(null);
        dialog.setLayout(new BorderLayout(10, 10));

        JLabel msg = new JLabel("Login success. What would you like to do?", SwingConstants.CENTER);
        msg.setBorder(new EmptyBorder(16, 16, 8, 16));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton invite = new JButton("Invite");
        JButton exit = new JButton("Exit");
        actions.add(invite);
        actions.add(exit);

        final boolean[] service = {false};

        invite.addActionListener(e -> {
            service[0] = true;
            dialog.dispose();
        });
        exit.addActionListener(e -> {
            service[0] = false;  // end up the app
            dialog.dispose();
        });

        dialog.add(msg, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.setVisible(true);
        return service[0];
    }


    // 3. Display outcomes
    private void showOutcomesDialog(Member currentMember, Member chosenMember) {
        JDialog dialog = new JDialog(frame, "Outcomes", true); // modal
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setSize(700, 520);
        dialog.setLocationRelativeTo(frame);

        StringBuilder sb = new StringBuilder();
        sb.append("============================== Outcomes ================================\n\n");
        sb.append("🤷‍♂️ Sender (").append(currentMember.getName()).append(")'s invitation records\n");
        sb.append("[Number of invitations sent] : ").append(currentMember.getDailyInvitationCount()).append("\n");
        sb.append("[Date of last invitation] : ").append(currentMember.getLastInvitationDate()).append("\n");
        sb.append(currentMember.getInvitationsSummary()).append("\n");
        sb.append("🤷‍♀️ Receiver (").append(chosenMember.getName()).append(")'s invitation records\n");
        sb.append(chosenMember.getInvitationsSummary());

        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JScrollPane scroll = new JScrollPane(ta);
        scroll.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dialog.dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(btnClose);

        dialog.setLayout(new BorderLayout());
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.add(south, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // endregion

    // region => Some small helpers for UI
    // 1. show specific members
    private void refreshTable(List<Member> list) {
        tableModel.setRowCount(0);
        for (Member m : list) {
            tableModel.addRow(new Object[]{m.getId(), m.getName(), m.getAge(), m.getGender(), m.getRegion()});
        }
    }

    // 2. show information
    private void info(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    // 3. show error
    private void error(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // 4. title with border
    private static class TitledBorderEx extends javax.swing.border.TitledBorder {
        public TitledBorderEx(String title) {
            super(BorderFactory.createLineBorder(new Color(220, 220, 220)), title);
            setTitleFont(getTitleFont().deriveFont(Font.BOLD));
        }
    }
    // endregion
}
