import java.awt.*;
import java.util.Random;

public class Ball {
    public double x, y;        // tâm
    public double vx, vy;      // vận tốc
    public int radius;
    public double mass;
    public Color color;
    public int id;
    public boolean active = true; // nếu false => bi đã rơi vào lỗ và không còn hiển thị/va chạm

    public Ball(int id, int x, int y, int radius, Color color) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.color = color;
        this.mass = radius * radius * Math.PI; // khối lượng tỉ lệ diện tích

        Random r = new Random();
        // cho vận tốc ngẫu nhiên
        this.vx = (r.nextDouble() * 6 - 3);
        this.vy = (r.nextDouble() * 6 - 3);
        if (Math.abs(vx) < 0.5) vx = Math.signum(vx) * 0.5 + (r.nextDouble() - 0.5);
        if (Math.abs(vy) < 0.5) vy = Math.signum(vy) * 0.5 + (r.nextDouble() - 0.5);
    }

    public void move(Rectangle bounds) {
        if (!active) return;
        x += vx;
        y += vy;

        // Va chạm với tường - phản xạ
        if (x - radius < bounds.x) {
            x = bounds.x + radius;
            vx = -vx;
        } else if (x + radius > bounds.x + bounds.width) {
            x = bounds.x + bounds.width - radius;
            vx = -vx;
        }

        if (y - radius < bounds.y) {
            y = bounds.y + radius;
            vy = -vy;
        } else if (y + radius > bounds.y + bounds.height) {
            y = bounds.y + bounds.height - radius;
            vy = -vy;
        }
    }

    public void draw(Graphics g) {
    g.setColor(color);
    g.fillOval((int)(x - radius), (int)(y - radius), radius * 2, radius * 2);

    // --- Tính độ sáng ---
    int brightness = (color.getRed() + color.getGreen() + color.getBlue()) / 3;

    // --- Chuẩn bị vẽ ID ---
    String s = String.valueOf(id);
    Font font = new Font("Arial", Font.BOLD, 14);
    g.setFont(font);
    FontMetrics fm = g.getFontMetrics();
    int tx = (int)(x - fm.stringWidth(s) / 2.0);
    int ty = (int)(y + fm.getAscent() / 2.0);

    Graphics2D g2 = (Graphics2D) g;
    g2.setFont(font);

    // --- Vẽ viền sáng quanh chữ để nổi bật ---
    g2.setStroke(new BasicStroke(2));
    g2.setColor(Color.WHITE);
    g2.drawString(s, tx, ty);

    // --- Vẽ chữ chính ---
    g2.setColor(Color.BLACK);
    g2.drawString(s, tx, ty);
    }

}
