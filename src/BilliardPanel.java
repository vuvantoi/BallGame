import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BilliardPanel extends JPanel implements Runnable {
    private final List<Ball> balls = new ArrayList<>();
    private boolean running = true;
    private final int borderThickness = 20; // 🔸 viền mỏng hơn (trước là 40)

    public BilliardPanel() {
        setBackground(new Color(102, 51, 0)); // màu nâu gỗ (chỉ để nền khi khởi tạo)

        Color[] colors = {
            Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
            Color.MAGENTA, Color.CYAN, Color.ORANGE, Color.PINK
        };

        Random r = new Random();
        int width = 800, height = 600;

        for (int i = 0; i < 8; i++) {
            int x = r.nextInt(width - 200) + 100;
            int y = r.nextInt(height - 200) + 100;
            balls.add(new Ball(i + 1, x, y, 20, colors[i]));
        }

        Thread t = new Thread(this);
        t.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Rectangle bounds = getBounds();

        // ===== VẼ BÀN BI-A =====
        Color borderColor = new Color(102, 51, 0); // 🔸 màu nâu gỗ
        Color clothColor = new Color(0, 120, 0);   // màu xanh mặt bàn

        // viền ngoài (nâu)
        g.setColor(borderColor);
        g.fillRect(0, 0, bounds.width, bounds.height);

        // mặt bàn (xanh)
        g.setColor(clothColor);
        g.fillRect(borderThickness, borderThickness,
                   bounds.width - borderThickness * 2,
                   bounds.height - borderThickness * 2);

        // đường viền trắng mảnh bên trong
        // g.setColor(Color.WHITE);
        // g.drawRect(borderThickness, borderThickness,
        //            bounds.width - borderThickness * 2,
        //            bounds.height - borderThickness * 2);

        // vẽ bóng
        for (Ball b : balls) {
            b.draw(g);
        }
    }

    @Override
    public void run() {
        while (running) {
            Rectangle playArea = new Rectangle(
                borderThickness,
                borderThickness,
                getWidth() - borderThickness * 2,
                getHeight() - borderThickness * 2
            );

            resolveCollisions();

            for (Ball b : balls) {
                b.move(playArea);
            }

            repaint();

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void resolveCollisions() {
        double restitution = 1.0;
        double percent = 0.8;
        double slop = 0.01;

        for (int i = 0; i < balls.size(); i++) {
            Ball A = balls.get(i);
            for (int j = i + 1; j < balls.size(); j++) {
                Ball B = balls.get(j);

                double dx = B.x - A.x;
                double dy = B.y - A.y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                double rSum = A.radius + B.radius;

                if (dist == 0.0) {
                    dx = (Math.random() - 0.5) * 0.01;
                    dy = (Math.random() - 0.5) * 0.01;
                    dist = Math.sqrt(dx * dx + dy * dy);
                }

                if (dist < rSum) {
                    double overlap = rSum - dist;
                    double correction = Math.max(overlap - slop, 0.0) / (1.0 / A.mass + 1.0 / B.mass);
                    double nx = dx / dist;
                    double ny = dy / dist;
                    double corrX = correction * nx * percent;
                    double corrY = correction * ny * percent;

                    A.x -= corrX / A.mass;
                    A.y -= corrY / A.mass;
                    B.x += corrX / B.mass;
                    B.y += corrY / B.mass;

                    double rvx = B.vx - A.vx;
                    double rvy = B.vy - A.vy;
                    double velAlongNormal = rvx * nx + rvy * ny;
                    if (velAlongNormal > 0) continue;

                    double impulse = -(1 + restitution) * velAlongNormal;
                    impulse = impulse / (1.0 / A.mass + 1.0 / B.mass);

                    double impulseX = impulse * nx;
                    double impulseY = impulse * ny;

                    A.vx -= impulseX / A.mass;
                    A.vy -= impulseY / A.mass;
                    B.vx += impulseX / B.mass;
                    B.vy += impulseY / B.mass;
                }
            }
        }
    }
}
