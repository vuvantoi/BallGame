import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class BilliardPanel extends JPanel implements Runnable {
    private ArrayList<Ball> balls = new ArrayList<>();
    private boolean running = true;

    public BilliardPanel() {
        setBackground(Color.BLACK);

        // Tạo 1 bóng ban đầu
        Random r = new Random();
        balls.add(new Ball(1, 100, 100, 20, Color.GREEN));

        Thread t = new Thread(this);
        t.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Rectangle bounds = getBounds();

        for (Ball b : balls) {
            b.draw(g);
        }

        // Vẽ khung
        g.setColor(Color.RED);
        g.drawRect(0, 0, bounds.width - 1, bounds.height - 1);
    }

    @Override
    public void run() {
        Rectangle bounds;
        while (running) {
            bounds = getBounds();

            for (Ball b : balls) {
                b.move(bounds);
            }

            repaint();

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {}
        }
    }
}
