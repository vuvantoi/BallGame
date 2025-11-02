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

        // khung
        g.setColor(Color.RED);
        g.drawRect(0, 0, bounds.width - 1, bounds.height - 1);

        for (Ball b : balls) {
            b.draw(g);
        }
    }

    @Override
    public void run() {
        Rectangle bounds;
        while (running) {
            bounds = getBounds();

            // 1) Kiểm tra va chạm giữa các cặp bóng (cập nhật vận tốc + positional correction)
            resolveCollisions();

            // 2) Di chuyển tất cả bóng (bao gồm phản xạ tường)
            for (Ball b : balls) {
                b.move(bounds);
            }

            repaint();

            try {
                Thread.sleep(16); // ~60 FPS, bạn có thể tăng lên 20ms nếu muốn
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void resolveCollisions() {
        double restitution = 1.0; // 1.0 = đàn hồi hoàn toàn; <1 mất năng lượng
        double percent = 0.8;     // tỉ lệ positional correction (0..1)
        double slop = 0.01;       // một ngưỡng nhỏ để tránh jitter khi chỉ chạm nhẹ

        for (int i = 0; i < balls.size(); i++) {
            Ball A = balls.get(i);
            for (int j = i + 1; j < balls.size(); j++) {
                Ball B = balls.get(j);

                double dx = B.x - A.x;
                double dy = B.y - A.y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                double rSum = A.radius + B.radius;

                if (dist == 0.0) {
                    // Tránh chia cho 0: dịch nhẹ ngẫu nhiên
                    dx = (Math.random() - 0.5) * 0.01;
                    dy = (Math.random() - 0.5) * 0.01;
                    dist = Math.sqrt(dx * dx + dy * dy);
                }

                if (dist < rSum) {
                    // --- positional correction (giải quyết overlap) ---
                    double overlap = rSum - dist;
                    double correction = Math.max(overlap - slop, 0.0) / (1.0 / A.mass + 1.0 / B.mass);
                    double nx = dx / dist;
                    double ny = dy / dist;
                    double corrX = correction * nx * percent;
                    double corrY = correction * ny * percent;
                    // Dịch A lùi, B tiến (tỉ lệ nghịch khối lượng)
                    A.x -= corrX / A.mass;
                    A.y -= corrY / A.mass;
                    B.x += corrX / B.mass;
                    B.y += corrY / B.mass;

                    // --- velocity impulse (elastic collision) ---
                    // tính relative velocity
                    double rvx = B.vx - A.vx;
                    double rvy = B.vy - A.vy;
                    // velocity along normal
                    double velAlongNormal = rvx * nx + rvy * ny;
                    // nếu đang tách ra (velAlongNormal > 0) thì bỏ qua
                    if (velAlongNormal > 0)
                        continue;
                    // tính impulse scalar
                    // tính impulse scalar
                    double impulse = -(1 + restitution) * velAlongNormal;
                    impulse = impulse / (1.0 / A.mass + 1.0 / B.mass);

                    // áp dụng impulse
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
