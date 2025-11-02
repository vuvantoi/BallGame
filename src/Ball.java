import java.awt.*;
import java.util.Random;

public class Ball {
    public double x, y;        // tâm (double để chính xác)
    public double vx, vy;      // vận tốc
    public int radius;
    public double mass;
    public Color color;
    public int id;

    public Ball(int id, int x, int y, int radius, Color color) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.color = color;
        this.mass = radius * radius * Math.PI; // khối lượng tỉ lệ diện tích (có thể chọn khác)

        Random r = new Random();
        // cho vận tốc ngẫu nhiên (không quá nhỏ)
        this.vx = (r.nextDouble() * 6 - 3);
        this.vy = (r.nextDouble() * 6 - 3);
        if (Math.abs(vx) < 0.5) vx = Math.signum(vx) * 0.5 + (r.nextDouble() - 0.5);
        if (Math.abs(vy) < 0.5) vy = Math.signum(vy) * 0.5 + (r.nextDouble() - 0.5);
    }

    public void move(Rectangle bounds) {
        x += vx;
        y += vy;

        // Va chạm với tường - phản xạ đơn giản
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
        g.setColor(Color.WHITE);
        // vẽ id ở giữa (cân chỉnh)
        FontMetrics fm = g.getFontMetrics();
        String s = String.valueOf(id);
        int tx = (int)(x - fm.stringWidth(s) / 2.0);
        int ty = (int)(y + fm.getAscent() / 2.0) ;
        g.drawString(s, tx, ty);
    }
}
