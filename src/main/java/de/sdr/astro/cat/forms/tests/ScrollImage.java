package de.sdr.astro.cat.forms.tests;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScrollImage extends JPanel {

    private static final long serialVersionUID = 1L;
    private BufferedImage image;
    private JPanel canvas;

    public ScrollImage() {
        try {
            this.image = ImageIO.read(this.getClass().getResourceAsStream("/skymap-kstars.png"));
        }catch(IOException ex) {
            Logger.getLogger(ScrollImage.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.canvas = new JPanel() {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0, null);
            }
        };

        canvas.add(new JButton("Currently I do nothing"));
        canvas.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        JScrollPane sp = new JScrollPane(canvas);
        setLayout(new BorderLayout());
        add(sp, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JPanel p = new ScrollImage();
                JFrame f = new JFrame();
                f.setContentPane(p);
                f.setSize(400, 300);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setVisible(true);
            }
        });
    }
}