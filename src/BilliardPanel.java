import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BilliardPanel extends JPanel implements Runnable {
    private final List<Ball> balls = new ArrayList<>();
    private boolean running = true;
    private final int borderThickness = 20; // 🔸 viền mỏng hơn (trước là 40)
    private final int holeRadius = 30; // bán kính lỗ ở giữa bàn
    private boolean firstFallOccurred = false; // đã có bi nào rơi chưa?
    private final int initWidth = 800, initHeight = 600; // kích thước dùng để đặt bi ban đầu

    public BilliardPanel() {
        setBackground(new Color(102, 51, 0)); // màu nâu gỗ (chỉ để nền khi khởi tạo)

        Color[] colors = {
            Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
            Color.MAGENTA, Color.CYAN, Color.ORANGE, Color.PINK
        };

        initBalls();

        Thread t = new Thread(this);
        t.start();
    }

    // Khởi tạo lại danh sách bi (có thể gọi để restart)
    private void initBalls() {
        synchronized (balls) {
            balls.clear();
            Random r = new Random();
            int width = initWidth, height = initHeight;
            Color[] colors = {
                Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
                Color.MAGENTA, Color.CYAN, Color.ORANGE, Color.PINK
            };
            for (int i = 0; i < 8; i++) {
                int x = r.nextInt(width - 200) + 100;
                int y = r.nextInt(height - 200) + 100;
                balls.add(new Ball(i + 1, x, y, 20, colors[i]));
            }
        }
    }

    // Reset toàn bộ mô phỏng: tạo lại bi, reset cờ, và khởi động lại vòng lặp
    private void resetSimulation() {
        firstFallOccurred = false;
        initBalls();
        // start a new simulation thread
        if (!running) {
            running = true;
            Thread t = new Thread(this);
            t.start();
        }
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

    // ===== VẼ LỖ Ở GIỮA BÀN =====
    int cx = bounds.x + bounds.width / 2;
    int cy = bounds.y + bounds.height / 2;
    // lỗ màu đen sâu
    g.setColor(Color.BLACK);
    g.fillOval(cx - holeRadius, cy - holeRadius, holeRadius * 2, holeRadius * 2);
    // viền nhẹ quanh lỗ
    g.setColor(new Color(30, 30, 30));
    g.drawOval(cx - holeRadius, cy - holeRadius, holeRadius * 2, holeRadius * 2);

        // đường viền trắng mảnh bên trong
        // g.setColor(Color.WHITE);
        // g.drawRect(borderThickness, borderThickness,
        //            bounds.width - borderThickness * 2,
        //            bounds.height - borderThickness * 2);

        // vẽ bóng
        synchronized (balls) {
            for (Ball b : balls) {
                if (b.active) b.draw(g);
            }
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

            // ignore collisions and motion for inactive balls
            synchronized (balls) {
                resolveCollisions();

                // move active balls
                for (Ball b : balls) {
                    if (b.active) b.move(playArea);
                }

                // kiểm tra bi rơi vào lỗ ở giữa
                int hx = getWidth() / 2;
                int hy = getHeight() / 2;
                List<Ball> toRemove = new ArrayList<>();
                for (Ball b : balls) {
                    if (!b.active) continue;
                    double dx = b.x - hx;
                    double dy = b.y - hy;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    // nếu tâm bi nằm trong lỗ (cho một khoảng đệm)
                    if (dist < (holeRadius - 4)) {
                        // đánh dấu là không hoạt động (rơi vào lỗ)
                        b.active = false;
                        // nếu đây là bi đầu tiên rơi thì dừng mô phỏng và hiện thông báo
                        if (!firstFallOccurred) {
                            firstFallOccurred = true;
                            int fallenId = b.id;
                            // dừng vòng lặp run
                            running = false;
                            // Hiện thông báo trên EDT
                            javax.swing.SwingUtilities.invokeLater(() -> {
                                // Hiện dialog với nút Restart để khởi động lại mô phỏng
                                Object[] options = {"Restart"};
                                int sel = javax.swing.JOptionPane.showOptionDialog(this,
                                    "Bi số " + fallenId + " đã rơi vào lỗ.",
                                    "Thông báo",
                                    javax.swing.JOptionPane.DEFAULT_OPTION,
                                    javax.swing.JOptionPane.INFORMATION_MESSAGE,
                                    null,
                                    options,
                                    options[0]);
                                if (sel == 0) {
                                    resetSimulation();
                                }
                            });
                        }
                        toRemove.add(b);
                    }
                }

                // loại bỏ các bi đã rơi (giúp giảm xử lý sau này)
                if (!toRemove.isEmpty()) {
                    balls.removeAll(toRemove);
                }
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
            if (!A.active) continue;
            for (int j = i + 1; j < balls.size(); j++) {
                Ball B = balls.get(j);
                if (!B.active) continue;

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
