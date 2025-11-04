import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BilliardPanel extends JPanel implements Runnable {
    private final List<Ball> balls = new ArrayList<>();
    private boolean running = true;
    private boolean firstFallOccurred = false; // đã có bi nào rơi chưa?
    private final MapConfig config;
    // Hình học đã tỷ lệ (tọa độ bản đồ -> pixel thực tế trên panel)
    private Rectangle[] scaledWalls = null;
    private Point[] scaledStarts = null;
    private Point[] scaledHoles = null;
    // Lưu trạng thái ban đầu của các bi (được tạo lần đầu) để restart dùng lại
    private List<BallSpec> initialSpecs = null;

    public BilliardPanel(MapConfig config) {
        this.config = config;
        setBackground(config.borderColor);
        // Defer initializing balls until paintComponent (so we have panel size and can scale positions)
        // Start simulation thread now; it will wait until scaled geometry is ready.
        Thread t = new Thread(this);
        t.start();
    }

    // Khởi tạo lại danh sách bi (có thể gọi để restart)
    private void initBalls() {
        // We defer creating initial ball positions until paintComponent (so scaling/offset are known).
        // Keep initBalls harmless: clear any existing balls and specs.
        synchronized (balls) {
            balls.clear();
            initialSpecs = null;
        }
    }

    // Mô tả đơn giản trạng thái ban đầu của một bi
    private static class BallSpec {
        int id;
        int x, y;
        int radius;
        Color color;

        BallSpec(int id, int x, int y, int radius, Color color) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.color = color;
        }
    }

    // Reset toàn bộ mô phỏng: tạo lại bi, reset cờ, và khởi động lại vòng lặp
    private void resetSimulation() {
        firstFallOccurred = false;
        // Clear existing specs so paintComponent will recreate balls with correct scaled coordinates
        synchronized (balls) {
            balls.clear();
            initialSpecs = null;
        }
        repaint();
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
        // viền ngoài (nâu)
        g.setColor(config.borderColor);
        g.fillRect(0, 0, bounds.width, bounds.height);

        // mặt bàn
        g.setColor(config.tableColor);
        g.fillRect(config.borderThickness, config.borderThickness,
                   bounds.width - config.borderThickness * 2,
                   bounds.height - config.borderThickness * 2);

        // Tính toán khu vực chơi (bên trong viền) và tỷ lệ từ cấu hình bản đồ sang pixel thực tế
        int playX = config.borderThickness;
        int playY = config.borderThickness;
        int playW = Math.max(10, bounds.width - config.borderThickness * 2);
        int playH = Math.max(10, bounds.height - config.borderThickness * 2);

        double sx = playW / (double) config.width;
        double sy = playH / (double) config.height;

        // prepare scaled walls and zones so drawing and collision use same coords
        if (config.walls != null) {
            scaledWalls = new Rectangle[config.walls.length];
            for (int i = 0; i < config.walls.length; i++) {
                Rectangle w = config.walls[i];
                int wx = playX + (int) Math.round(w.x * sx);
                int wy = playY + (int) Math.round(w.y * sy);
                int ww = (int) Math.round(w.width * sx);
                int wh = (int) Math.round(w.height * sy);
                scaledWalls[i] = new Rectangle(wx, wy, ww, wh);
            }
        } else {
            scaledWalls = null;
        }

        if (config.startZones != null && config.startZones.length > 0) {
            scaledStarts = new Point[config.startZones.length];
            for (int i = 0; i < config.startZones.length; i++) {
                scaledStarts[i] = new Point(playX + (int) Math.round(config.startZones[i].x * sx),
                                            playY + (int) Math.round(config.startZones[i].y * sy));
            }
        } else {
            // phương án dự phòng: đặt ở trung tâm khu vực chơi
            scaledStarts = new Point[] { new Point(playX + playW/2, playY + playH/2) };
        }
        if (config.holes != null) {
            scaledHoles = new Point[config.holes.length];
            for (int i = 0; i < config.holes.length; i++) {
                scaledHoles[i] = new Point(playX + (int) Math.round(config.holes[i].x * sx),
                                           playY + (int) Math.round(config.holes[i].y * sy));
            }
        } else {
            scaledHoles = null;
        }

        // Nếu chưa tạo initialSpecs (constructor chạy trước khi panel có kích thước), tạo chúng bây giờ với tọa độ đã tỷ lệ
        if (initialSpecs == null && getWidth() > 0 && getHeight() > 0) {
            synchronized (balls) {
                balls.clear();
                initialSpecs = new ArrayList<>();
                Random r = new Random();
                Color[] colors = {
                    Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW,
                    Color.MAGENTA, Color.CYAN, Color.ORANGE, Color.PINK
                };
                int startRadius = (int) Math.round(config.holeRadius * 2 * Math.max(sx, sy));
                int zones = (scaledStarts != null && scaledStarts.length > 0) ? scaledStarts.length : 1;
                for (int i = 0; i < config.numBalls; i++) {
                    double angle = r.nextDouble() * 2 * Math.PI;
                    double dist = r.nextDouble() * startRadius;
                    Point center = scaledStarts[i % zones];
                    int x = center.x + (int) (Math.cos(angle) * dist);
                    int y = center.y + (int) (Math.sin(angle) * dist);
                    int radius = (int) Math.max(4, config.ballRadius * Math.max(sx, sy));
                    Color c = colors[i % colors.length];
                    balls.add(new Ball(i + 1, x, y, radius, c));
                    initialSpecs.add(new BallSpec(i + 1, x, y, radius, c));
                }
            }
        }

        // ===== VẼ TƯỜNG =====
        if (scaledWalls != null) {
            g.setColor(new Color(139, 69, 19));  // màu nâu đậm cho tường
            for (Rectangle wall : scaledWalls) {
                g.fillRect(wall.x, wall.y, wall.width, wall.height);
                // Vẽ viền đen mỏng để tường nổi bật
                g.setColor(Color.BLACK);
                g.drawRect(wall.x, wall.y, wall.width, wall.height);
                g.setColor(new Color(139, 69, 19));
            }
        }

        // ===== VẼ LỖ ĐÍCH (có thể nhiều lỗ) =====
        int holeR = (int) Math.round(config.holeRadius * Math.max(sx, sy));
        if (scaledHoles != null) {
            g.setColor(Color.BLACK);
            for (Point p : scaledHoles) {
                g.fillOval(p.x - holeR, p.y - holeR, holeR * 2, holeR * 2);
            }
            // viền nhẹ quanh các lỗ
            g.setColor(new Color(30, 30, 30));
            for (Point p : scaledHoles) {
                g.drawOval(p.x - holeR, p.y - holeR, holeR * 2, holeR * 2);
            }
        }

        // ===== VẼ VÙNG XUẤT PHÁT (có thể nhiều vùng) =====
        g.setColor(new Color(255, 255, 0, 100));  // màu vàng nhạt, trong suốt
        int startRadius = (int) Math.round(config.holeRadius * 2 * Math.max(sx, sy));
        if (scaledStarts != null) {
            for (Point c : scaledStarts) {
                g.fillOval(c.x - startRadius, c.y - startRadius, startRadius * 2, startRadius * 2);
            }
        }

        synchronized (balls) {
            for (Ball b : balls) {
                if (b.active) b.draw(g);
            }
        }
    }

    @Override
    public void run() {
        // Wait until paintComponent sets up scaledHoles/scaledStarts/scaledWalls
        while ((scaledStarts == null || scaledStarts.length == 0) && running) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        while (running) {
            Rectangle playArea = new Rectangle(
                config.borderThickness,
                config.borderThickness,
                getWidth() - config.borderThickness * 2,
                getHeight() - config.borderThickness * 2
            );

            // ignore collisions and motion for inactive balls
            synchronized (balls) {
                resolveCollisions();

                // move active balls
                for (Ball b : balls) {
                    if (b.active) b.move(playArea);
                }

                // kiểm tra va chạm với tường (sử dụng scaledWalls nếu có)
                if (scaledWalls != null) {
                    for (Ball b : balls) {
                        if (!b.active) continue;
                        for (Rectangle wall : scaledWalls) {
                            // Kiểm tra va chạm với từng cạnh của tường
                            if (b.x + b.radius > wall.x && b.x - b.radius < wall.x + wall.width &&
                                b.y + b.radius > wall.y && b.y - b.radius < wall.y + wall.height) {

                                // Tính toán overlap với các cạnh
                                double overlapLeft = b.x + b.radius - wall.x;
                                double overlapRight = wall.x + wall.width - (b.x - b.radius);
                                double overlapTop = b.y + b.radius - wall.y;
                                double overlapBottom = wall.y + wall.height - (b.y - b.radius);

                                double minOverlap = Math.min(Math.min(overlapLeft, overlapRight),
                                                            Math.min(overlapTop, overlapBottom));

                                double restitution = 0.9; // năng lượng giữ lại khi va chạm với tường
                                if (minOverlap == overlapLeft) {
                                    b.x = wall.x - b.radius;
                                    b.vx = -b.vx * restitution;
                                } else if (minOverlap == overlapRight) {
                                    b.x = wall.x + wall.width + b.radius;
                                    b.vx = -b.vx * restitution;
                                } else if (minOverlap == overlapTop) {
                                    b.y = wall.y - b.radius;
                                    b.vy = -b.vy * restitution;
                                } else {
                                    b.y = wall.y + wall.height + b.radius;
                                    b.vy = -b.vy * restitution;
                                }

                                // slight friction upon hitting wall
                                b.vx *= 0.98;
                                b.vy *= 0.98;
                            }
                        }
                    }
                }
                // kiểm tra bi rơi vào bất kỳ lỗ nào
                int holeR = (int) Math.round(config.holeRadius * Math.max(getWidth() / (double) config.width,
                                                                             getHeight() / (double) config.height));
                List<Ball> toRemove = new ArrayList<>();
                for (Ball b : balls) {
                    if (!b.active) continue;
                    boolean fell = false;
                    if (scaledHoles != null) {
                        for (Point h : scaledHoles) {
                            double dx = b.x - h.x;
                            double dy = b.y - h.y;
                            double dist = Math.sqrt(dx * dx + dy * dy);
                            if (dist < (holeR - 4)) {
                                fell = true;
                                break;
                            }
                        }
                    }
                    if (fell) {
                        // đánh dấu là không hoạt động (rơi vào lỗ)
                        b.active = false;
                        // nếu đây là bi đầu tiên rơi thì dừng mô phỏng và hiện thông báo
                        if (!firstFallOccurred) {
                            firstFallOccurred = true;
                            int fallenId = b.id;
                            // dừng vòng lặp run
                            running = false;
                            // Hiện thông báo trên EDT
                            SwingUtilities.invokeLater(() -> {
                                String[] options = {"Chơi lại", "Quay lại Menu"};
                                int choice = JOptionPane.showOptionDialog(
                                    SwingUtilities.getWindowAncestor(this),  // parent window
                                    "Bi số " + fallenId + " đã rơi vào lỗ.",
                                    "Kết thúc lượt",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.INFORMATION_MESSAGE,
                                    null,
                                    options,
                                    options[0]
                                );
                                
                                if (choice == JOptionPane.YES_OPTION) {
                                    // Nút "Chơi lại"
                                    resetSimulation();
                                } else {
                                    // Nút "Quay lại Menu"
                                    JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
                                    frame.getContentPane().removeAll();
                                    frame.setSize(800, 600);
                                    frame.add(new MenuPanel(frame));
                                    frame.setLocationRelativeTo(null);  // Căn giữa cửa sổ
                                    frame.revalidate();
                                    frame.repaint();
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