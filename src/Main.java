import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Billiard Game - Chọn Bàn Chơi");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.add(new MenuPanel(frame));
        frame.setLocationRelativeTo(null); // căn giữa màn hình
        frame.setVisible(true);
        frame.setResizable(false);
    }
}
