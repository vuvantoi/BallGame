import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BilliardPanel extends JPanel implements Runnable {
    private final List<Ball> balls = new ArrayList<>();
    private boolean running = true;

    public BilliardPanel() {
        setBackground(Color.BLACK);

        // Tạo 8 màu khác nhau
        Color[] colors = {
            Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
            Color.MAGENTA, Color.CYAN, Color.ORANGE, Color.PINK
        };

        Random r = new Random();
        int width = 800, height = 600;

        // Tạo 8 quả bóng
        for (int i = 0; i < 8; i++) {
            int x = r.nextInt(width - 100) + 50;
            int y = r.nextInt(height - 100) + 50;
            balls.add(new Ball(i + 1, x, y, 20, colors[i]));
        }

        // Khởi động luồng mô phỏng
        Thread t = new Thread(this);
        t.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Rectangle bounds = getBounds();

        // Vẽ khung đỏ
        g.setColor(Color.RED);
        g.drawRect(0, 0, bounds.width - 1, bounds.height - 1);

        // Vẽ từng bóng
        for (Ball b : balls) {
            b.draw(g);
        }
    }

    @Override
    public void run() {
        Rectangle bounds;
        while (running) {
            bounds = getBounds();

            // Cập nhật vị trí tất cả bóng
            for (Ball b : balls) {
                b.move(bounds);
            }

            repaint();

            try {
                Thread.sleep(20); // ~50 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
