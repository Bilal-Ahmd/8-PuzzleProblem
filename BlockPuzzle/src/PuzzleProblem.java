import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.List;

public class PuzzleProblem {

    public static class TreeNode {
        public String val;
        public List<TreeNode> children = new LinkedList<>();
        public TreeNode parent;
        public int depth = 0;
        public int fn = 0;

        TreeNode(String data, TreeNode par) {
            val = data;
            parent = par;
            if (parent != null)
                depth = parent.depth + 1;
            fn = f(this);
        }
    }

    public static int speedInterval = 0;
    public final static String finalState = " 12345678";

    public static TreeNode root;
    public static HashSet<String> visited = new HashSet<>();

    public static JFrame frame;
    public static GUI gui;

    public static void createGUI() throws ClassNotFoundException,
            UnsupportedLookAndFeelException, InstantiationException,
            IllegalAccessException {
        frame = new JFrame("8-Puzzle");
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        gui = new GUI();
    }

    public static void setGUI() {
        gui.setProperties();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setResizable(false);
        frame.setContentPane(gui.getRoot());
        frame.setVisible(true);
    }

    public static void printSolution(List<String> moves) {
        if (moves == null) {
            gui.move(root.val);
            gui.SolutionNotPossibleException();
            return;
        }
        for (String move : moves) {
            gui.move(move);
            try {
                Thread.sleep(speedInterval);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        gui.totalMoves(moves.size() - 1);
    }

    public static List<String> getValidMoves(String state) {
        List<String> states = new LinkedList<>();
        int[] offsets = {1, -1, 3, -3};
        int hole = state.indexOf(' ');
        for (int offset : offsets) {
            StringBuilder sb = new StringBuilder(state);
            if (((hole + 1) % 3 == 0 && offset == 1) || ((hole % 3) == 0 && offset == -1))
                continue;
            int j = hole + offset;
            if (j >= 0 && j < 9) {
                char l = sb.charAt(hole), r = sb.charAt(j);
                sb.setCharAt(hole, r);
                sb.setCharAt(j, l);
                states.add(sb.toString());
            }
        }
        return states;
    }

    public static void makeChildren(TreeNode node) {
        List<String> moves = getValidMoves(node.val);
        for (String move : moves) {
            if (!visited.contains(move)) {
                TreeNode child = new TreeNode(move, node);
                node.children.add(child);
                visited.add(move);
            }
        }
    }

    public static int h(TreeNode n){
        int count = 0;
        for(int i = 0; i < finalState.length(); ++i)
            if(n.val.charAt(i) != finalState.charAt(i))
                ++count;
        return count;
    }

    public static int g(TreeNode n){
        return n.depth;
    }

    public static int f(TreeNode n){
        return g(n) + h(n);
    }

    public static List<String> tracePath(TreeNode node) {
        List<String> path = new LinkedList<>();
        while (node != null) {
            path.add(0, node.val);
            node = node.parent;
        }
        return path;
    }

    public static List<String> A_Star() {
        if (root == null)
            return null;

        PriorityQueue<TreeNode> frontier = new PriorityQueue<>(1, Comparator.comparingInt(a -> a.fn));
        frontier.add(root);
        while (!frontier.isEmpty()) {
            TreeNode node = frontier.poll();
            if (node.val.equals(finalState))
                return tracePath(node);
            makeChildren(node);
            frontier.addAll(node.children);
        }
        return null;
    }

    public static void readFromFile(String filename) {
        File file = new File(filename);
        try {
            Scanner scanner = new Scanner(file);
            String string = scanner.nextLine();
            string += scanner.nextLine();
            string += scanner.nextLine();
            for (char c : finalState.toCharArray()) {
                if (string.indexOf(c) == -1 || string.length() > 9) {
                    gui.InvalidInitialStateException();
                    System.exit(1);
                }
            }
            root = new TreeNode(string, null);
            visited.add(root.val);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            gui.FileNotFoundException();
            System.exit(1);
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            gui.InvalidInitialStateException();
            System.exit(1);
        }
    }

    public static String[] preference() {
        JPanel fields = new JPanel(new GridLayout(2, 1));
        JComboBox<String> comboBox2 = new JComboBox<>(new String[]{"Fast", "Slow", "Instant"});

        fields.add(comboBox2);

        int dialog = JOptionPane.showConfirmDialog(null, fields, "Simulation Speed", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (dialog != 0)
            System.exit(0);
        String[] result = new String[2];
        result[1] = comboBox2.getSelectedItem().toString();
        return result;
    }

    public static void main(String[] args) {
        try {
            createGUI();
        } catch (IllegalAccessException |
                InstantiationException |
                UnsupportedLookAndFeelException |
                ClassNotFoundException e) {
            e.printStackTrace();
        }

        readFromFile("File.txt");
        String[] selection = preference();
        setGUI();

        switch (selection[1]) {
            case "Fast":
                speedInterval = 250;
                break;
            case "Slow":
                speedInterval = 600;
                break;
            case "Instant":
                speedInterval = 0;
                break;
        }
        System.out.println(A_Star());
        printSolution(A_Star());
    }
}