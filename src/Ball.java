import java.awt.*;
import java.util.Random;

public class Ball {
    public int x, y;           // Vị trí tâm
    public int radius;         // Bán kính
    public int dx, dy;         // Vận tốc
    public Color color;        // Màu
    public int id;             // Số thứ tự

    public Ball(int id, int x, int y, int radius, Color color) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.color = color;
        Random r = new Random();
        dx = r.nextInt(7) + 2;   // tốc độ ngẫu nhiên
        dy = r.nextInt(7) + 2;
    }

    public void move(Rectangle bounds) {
        x += dx;
        y += dy;

        // Nảy khi chạm tường
        if (x - radius < bounds.x || x + radius > bounds.x + bounds.width) {
            dx = -dx;
        }
        if (y - radius < bounds.y || y + radius > bounds.y + bounds.height) {
            dy = -dy;
        }
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        g.setColor(Color.WHITE);
        g.drawString(String.valueOf(id), x - 4, y + 4);
    }
}
