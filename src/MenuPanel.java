import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MenuPanel extends JPanel {
    private final JFrame parentFrame;

    public MenuPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        
        // Panel chính giữa chứa các nút
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(new Color(0, 80, 0));  // xanh sẫm
        
        // Tiêu đề
        JLabel titleLabel = new JLabel("Billiard Game");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Tạo các nút cho từng map
        JButton[] mapButtons = new JButton[3];
        MapConfig[] maps = {MapConfig.MAP1, MapConfig.MAP2, MapConfig.MAP3};
        
        for (int i = 0; i < 3; i++) {
            mapButtons[i] = createMapButton(maps[i]);
        }
        
        // Thêm các thành phần vào panel
        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(50));
        
        for (JButton btn : mapButtons) {
            centerPanel.add(btn);
            centerPanel.add(Box.createVerticalStrut(20));
        }
        
        centerPanel.add(Box.createVerticalGlue());
        
        // Thêm panel vào frame
        add(centerPanel, BorderLayout.CENTER);
    }
    
    private JButton createMapButton(MapConfig map) {
        JButton button = new JButton(map.name);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Tùy chỉnh giao diện nút
        button.setPreferredSize(new Dimension(300, 60));
        button.setMaximumSize(new Dimension(300, 60));
        button.setBackground(new Color(200, 200, 200));
        button.setFocusPainted(false);
        
        // Xử lý sự kiện click
        button.addActionListener(e -> switchToGame(map));
        
        return button;
    }
    
    private void switchToGame(MapConfig map) {
        parentFrame.getContentPane().removeAll();
        parentFrame.setSize(map.width, map.height);
        parentFrame.add(new BilliardPanel(map));
        parentFrame.setLocationRelativeTo(null);  // Căn giữa cửa sổ
        parentFrame.revalidate();
        parentFrame.repaint();
    }
}