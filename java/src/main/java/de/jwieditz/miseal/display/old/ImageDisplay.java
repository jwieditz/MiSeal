package de.jwieditz.miseal.display.old;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.jwieditz.miseal.Minutia;

public class ImageDisplay implements MouseMotionListener {

    public static final int DEFAULT_SCALE = 2;

    private static final DecimalFormat format = new DecimalFormat("####0.00000");

    protected double[][] data;
    protected int scale;
    protected double minVal;
    protected double maxVal;

    private Minutia[] minutiae;
    private boolean showMinutiae;

    private JLabel mouseInfo;

    public ImageDisplay(double[][] data) {
        this(data, DEFAULT_SCALE);
    }

    public ImageDisplay(double[][] data, int scale) {
        this.data = data;
        this.scale = scale;

        double[] minMax = getMinMax(data);
        minVal = minMax[0];
        maxVal = minMax[1];
    }

    public ImageDisplay withMinutiae(Minutia[] minutiae) {
        this.minutiae = minutiae;
        showMinutiae = true;
        return this;
    }

    public void display(String title) {
        //        int w = data.length;
        //        int h = data[0].length;

        BufferedImage img = drawImageToDisplay();

        JFrame frame = new JFrame("Image " + title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setPreferredSize(new Dimension(img.getWidth(), img.getHeight() + 90));
        JPanel panel = new JPanel() {

            @Override
            public void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.clearRect(0, 0, getWidth(), getHeight());
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                g2d.drawImage(img, 0, 0, this);

                if (minutiae != null && showMinutiae) {
                    g2d.setColor(Color.green);
                    g2d.setStroke(new BasicStroke((int) (1.5 * scale)));
                    for (Minutia minutia : minutiae) {
                        int x = minutia.getX();
                        int y = minutia.getY();
                        double or = -minutia.getOrientation();
                        int w = 7 * scale;
                        int h = 7 * scale;
                        g2d.drawOval(x * scale - w / 2, y * scale - h / 2, 7 * scale, 7 * scale);
                        g2d.drawLine((int) (x * scale + w / 2 * Math.cos(or)), (int) (y * scale + h / 2 * Math.sin(or)), (int) (x * scale + 15 * Math.cos(or)), (int) (y * scale + 15 * Math.sin(or)));
                    }
                }
            }
        };
        panel.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
        panel.addMouseMotionListener(this);
        mouseInfo = new JLabel();
        mouseInfo.setPreferredSize(new Dimension(img.getWidth(), 30));
        mouseInfo.setFont(Font.getFont(Font.MONOSPACED));
        mouseInfo.setBorder(new EmptyBorder(0, 50, 0, 0));

        JPanel legend = new JPanel() {

            @Override
            public void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.clearRect(0, 0, getWidth(), getHeight());
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                // draw legend color bar
                for (int i = 50; i < getWidth() - 50; i++) {
                    float c = (float) (i - 50) / (getWidth() - 100);
                    g2d.setColor(new Color(c, c, c));
                    g2d.drawLine(i, 0, i, 25);
                }

                // draw legend labels
                g2d.setColor(Color.black);
                g2d.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
                for (int i = 0; i <= 10; i++) {
                    String label = String.format("%.3f", minVal + (maxVal - minVal) * i / 10);
                    int labelWidth = g2d.getFontMetrics().stringWidth(label);

                    int xPos = 50 + (getWidth() - 100) * i / 10;

                    g2d.drawString(label, xPos - labelWidth / 2, 45);

                    g2d.drawLine(xPos, 26, xPos, 45 - g2d.getFontMetrics().getHeight() / 2 - 5);
                }
            }
        };
        legend.setPreferredSize(new Dimension(mainPanel.getWidth(), 60));

        mainPanel.add(panel);
        mainPanel.add(mouseInfo);
        mainPanel.add(legend);

        frame.getContentPane().add(mainPanel);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        JMenuItem menuItem = new JMenuItem("Save to...", KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
        menuItem.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Specify a file to save");
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir"), ".."));
                fileChooser.setFileFilter(new FileNameExtensionFilter("PNG files", "png"));

                int userSelection = fileChooser.showSaveDialog(frame);

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File outputFile = fileChooser.getSelectedFile();
                    try {
                        BufferedImage img = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
                        panel.paint(img.getGraphics());
                        ImageIO.write(img, "png", outputFile);
                    } catch (IOException e) {
                        System.err.println("Could not save image to " + outputFile);
                        e.printStackTrace();
                    }
                }
            }
        });
        menu.add(menuItem);

        JMenu imageMenu = new JMenu("Image");
        imageMenu.setMnemonic(KeyEvent.VK_I);

        JMenuItem toggleMinutiaeItem = new JMenuItem("Toggle minutiae", KeyEvent.VK_M);
        toggleMinutiaeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_MASK));
        toggleMinutiaeItem.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                showMinutiae = !showMinutiae;
                mainPanel.repaint();
            }
        });
        imageMenu.add(toggleMinutiaeItem);

        menuBar.add(menu);
        menuBar.add(imageMenu);
        frame.setJMenuBar(menuBar);

        frame.pack();
        frame.setVisible(true);
    }

    protected BufferedImage drawImageToDisplay() {
        int w = data.length;
        int h = data[0].length;

        BufferedImage img = new BufferedImage(scale * w, scale * h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                g.setColor(getNormalizedColor(data[i][j]));
                g.fillRect(scale * i, scale * j, scale, scale);
            }
        }
        return img;
    }

    protected Color getNormalizedColor(double d) {
        if (Double.isNaN(d)) {
            return new Color(105, 0, 0);
        }
        d -= minVal;
        d /= (maxVal - minVal);
        float c = (float) d;
        return new Color(c, c, c);
    }

    protected String getDisplayInformationAt(int x, int y) {
        double value = data[x][y];
        double normalizedValue = (value - minVal) / (maxVal - minVal);
        String valueFormatted = format.format(value);
        if (Double.isNaN(value)) {
            valueFormatted = "NaN";
        }
        String normalizedValueFormatted = format.format(normalizedValue);
        if (Double.isNaN(normalizedValue)) {
            normalizedValueFormatted = "NaN";
        }
        return "original value = " + valueFormatted + "  |  normalized (displayed) value = " + normalizedValueFormatted;
    }

    private double[] getMinMax(double[][] data) {
        double[] minMax = new double[2];
        double min = 0, max = 0;
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                if (Double.isInfinite(data[i][j])) {
                    continue;
                }
                if (data[i][j] < min) {
                    min = data[i][j];
                } else if (data[i][j] > max) {
                    max = data[i][j];
                }
            }
        }
        minMax[0] = min;
        minMax[1] = max;
        return minMax;
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {}

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        int x = mouseEvent.getX() / scale;
        int y = mouseEvent.getY() / scale;
        if (x >= 0 && x < data.length && y >= 0 && y < data[0].length) {
            mouseInfo.setText(String.format("x = %3d  |  y = %3d  |  %s", x, y, getDisplayInformationAt(x, y)));
        }
    }
}
