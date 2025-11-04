import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Billiard Simulation - Bàn Bi-a");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1400, 750);
        frame.add(new BilliardPanel());
        frame.setVisible(true);
        frame.setResizable(false);
    }
}
