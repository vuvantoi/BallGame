import java.awt.*;

public class MapConfig {
    public final int width;
    public final int height;
    public final int borderThickness;
    public final int holeRadius;
    public final int ballRadius;
    public final int numBalls;
    public final Color tableColor;
    public final Color borderColor;
    public final String name;
    public final Point[] startZones;  // Các vùng xuất phát (có thể nhiều vùng)
    public final Point[] holes;       // Các vị trí lỗ (có thể nhiều lỗ)
    public final Rectangle[] walls;    // Mảng tường cản (null nếu không có)

    public MapConfig(String name, int width, int height, int borderThickness, 
                    int holeRadius, int ballRadius, int numBalls,
                    Color tableColor, Color borderColor,
                    Point[] startZones, Point[] holes, Rectangle[] walls) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.borderThickness = borderThickness;
        this.holeRadius = holeRadius;
        this.ballRadius = ballRadius;
        this.numBalls = numBalls;
        this.tableColor = tableColor;
        this.borderColor = borderColor;
        this.startZones = startZones;
        this.holes = holes;
        this.walls = walls;
    }

    // Ba map mẫu với cấu hình khác nhau
    public static final MapConfig MAP1 = new MapConfig(
        "Bàn Tiêu Chuẩn",
        1400, 750,      // size
        30, 30, 16,     // border, hole, ball
        200,            // số bi
        new Color(0, 120, 0),    // xanh lá đậm
        new Color(102, 51, 0),   // nâu gỗ
        new Point[] { new Point(700, 375) },     // xuất phát tại tâm bàn
        new Point[] {            // bốn lỗ ở bốn góc (map coords)
            new Point(60, 60),
            new Point(1340, 60),
            new Point(60, 690),
            new Point(1340, 690)
        },
        null                     // không có tường
    );

    public static final MapConfig MAP2 = new MapConfig(
        "Bàn Mini",
        800, 600,       // nhỏ hơn
        20, 20, 12,     // viền mỏng hơn
        100,            // ít bi hơn
        new Color(0, 100, 100),  // xanh ngọc
        new Color(70, 35, 0),    // nâu đậm
        new Point[] { new Point(60, 60), new Point(740, 60), new Point(60, 540), new Point(740, 540) }, // start ở 4 góc
        new Point[] { new Point(400, 300) }, // lỗ ở giữa
        null                     // không có tường
    );

    public static final MapConfig MAP3 = new MapConfig(
            "Đại Sảnh",
            1400, 800,      // kích thước vừa phải
            35, 32, 16,     // viền và bi vừa phải
            150,            // giảm số bi xuống để dễ quan sát
            new Color(0, 80, 0),     // xanh sẫm
            new Color(139, 69, 19),  // nâu đỏ
            new Point[] { new Point(100, 700) },     // xuất phát ở góc dưới trái
            new Point[] { new Point(1300, 100) }, // lỗ ở góc trên phải
            new Rectangle[] {        // các tường rải rác khắp bàn
                new Rectangle(200, 150, 300, 20),   // ngang cao
                new Rectangle(600, 120, 20, 200),   // dọc trên trái
                new Rectangle(900, 80, 20, 220),    // dọc trên phải
                new Rectangle(300, 360, 200, 20),   // ngang giữa trái
                new Rectangle(700, 360, 20, 220),   // dọc giữa
                new Rectangle(1000, 360, 300, 20),  // ngang giữa phải
                new Rectangle(550, 560, 300, 20),   // ngang dưới giữa
                new Rectangle(950, 560, 20, 160),   // dọc dưới phải
                new Rectangle(400, 240, 160, 20)    // tấm nhỏ rải rác
            }
    );
}