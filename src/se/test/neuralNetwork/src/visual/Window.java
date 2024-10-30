package se.test.neuralNetwork.src.visual;

import se.test.neuralNetwork.src.visual.utils.ScalableIcon;
import se.test.neuralNetwork.examples.pendulum.GameLoop;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Window extends JFrame {

    private final DrawPanel drawPanel;
    private final GameLoop gameLoop = new GameLoop(60, true, false);
    private final Content content;
    private final int panelWidth = 960;
    private final int panelHeight = 540;
    private final Graphics2D graphics2D;
    private final BufferedImage tempScreen;
    private double currentScale = 1.0;
    private final HashMap<Component, OriginalComponentValues> componentList = new HashMap<>();
    private final int[] mousePos = new int[]{0, 0};

    private final List<ClickListener> clickListeners = new ArrayList<>();

    public void addClickListener(ClickListener listener) {
        clickListeners.add(listener);
    }

    public interface ClickListener {
        void onClick();
    }

    private Runnable onClose;

    public Window(Content content, String title, boolean closeProgramOnClose) {
        super(title);

        this.content = content;

        tempScreen = new BufferedImage(panelWidth, panelHeight, BufferedImage.TYPE_INT_ARGB);
        graphics2D = (Graphics2D) tempScreen.getGraphics();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (onClose != null) {
                    onClose.run();
                }
                content.close();
                if (closeProgramOnClose) {
                    System.exit(0);
                } else {
                    dispose();
                }
            }
        });
        setSize(panelWidth, panelHeight);

        drawPanel = new DrawPanel();

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(drawPanel, gbc);

        centerPanel.setSize(5000, 5000);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(centerPanel, BorderLayout.CENTER);

        setLocationRelativeTo(null);
        setVisible(true);
        gameLoop.addUpdateListener(content::update);
        gameLoop.addRenderListener(() -> {
            content.render();
            drawPanel.repaint();
        });


        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

        GraphicsDevice[] screens = ge.getScreenDevices();

        int screenIndex = 0;

        if (screenIndex >= 0 && screenIndex < screens.length) {
            Rectangle screenBounds = screens[screenIndex].getDefaultConfiguration().getBounds();

            setSize(screenBounds.width - 600, screenBounds.height - 400);

            setLocation(screenBounds.x + screenBounds.width / 2 - getWidth() / 2,
                    screenBounds.y + screenBounds.height / 2 - getHeight() / 2);
        } else {
            setLocationRelativeTo(null);
        }


        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);

                setScale();

                scaleComponents();

            }
        });
        centerPanel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Insets insets = getInsets();
                int contentWidth = getWidth() - insets.left - insets.right;
                int contentHeight = getHeight() - insets.top - insets.bottom;

                mousePos[0] = (int) ((e.getX() - Math.max(0, contentWidth - panelWidth * currentScale) / 2) / currentScale);
                mousePos[1] = (int) ((e.getY() - Math.max(0, contentHeight - panelHeight * currentScale) / 2) / currentScale);

            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Insets insets = getInsets();
                int contentWidth = getWidth() - insets.left - insets.right;
                int contentHeight = getHeight() - insets.top - insets.bottom;

                mousePos[0] = (int) ((e.getX() - Math.max(0, contentWidth - panelWidth * currentScale) / 2) / currentScale);
                mousePos[1] = (int) ((e.getY() - Math.max(0, contentHeight - panelHeight * currentScale) / 2) / currentScale);

            }
        });

        centerPanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                for (ClickListener clickListener : clickListeners) {
                    clickListener.onClick();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        setScale();

        scaleComponents();
    }

    private void setScale() {
        Insets insets = getInsets();
        int contentWidth = getWidth() - insets.left - insets.right;
        int contentHeight = getHeight() - insets.top - insets.bottom;

        currentScale = Math.min((double) contentWidth / panelWidth, (double) contentHeight / panelHeight);
    }

    private void scaleComponents() {
        for (Component comp : drawPanel.getComponents()) {
            OriginalComponentValues values = componentList.get(comp);
            if (values != null) {
                comp.setBounds(
                        (int) (values.getX() * currentScale),
                        (int) (values.getY() * currentScale),
                        (int) (values.getW() * currentScale),
                        (int) (values.getH() * currentScale)
                );
                if (comp instanceof AbstractButton button) {

                    ScalableIcon icon = (ScalableIcon) button.getIcon();
                    if (icon != null) {
                        icon.changeSize((int) (values.getW() * currentScale), (int) (values.getH() * currentScale));
                        button.setIcon(icon);
                    }

                    ScalableIcon rolloverIcon = (ScalableIcon) button.getRolloverIcon();
                    if (rolloverIcon != null) {
                        rolloverIcon.changeSize((int) (values.getW() * currentScale), (int) (values.getH() * currentScale));
                        button.setRolloverIcon(rolloverIcon);
                    }

                    ScalableIcon disabledIcon = (ScalableIcon) button.getDisabledIcon();
                    if (disabledIcon != null) {
                        disabledIcon.changeSize((int) (values.getW() * currentScale), (int) (values.getH() * currentScale));
                        button.setDisabledIcon(disabledIcon);
                    }

                    ScalableIcon selectedIcon = (ScalableIcon) button.getSelectedIcon();
                    if (selectedIcon != null) {
                        selectedIcon.changeSize((int) (values.getW() * currentScale), (int) (values.getH() * currentScale));
                        button.setSelectedIcon(selectedIcon);
                    }

                    ScalableIcon rolloverSelectedIcon = (ScalableIcon) button.getRolloverSelectedIcon();
                    if (rolloverSelectedIcon != null) {
                        rolloverSelectedIcon.changeSize((int) (values.getW() * currentScale), (int) (values.getH() * currentScale));
                        button.setRolloverIcon(rolloverSelectedIcon);
                    }

                    ScalableIcon disabledSelectedIcon = (ScalableIcon) button.getDisabledSelectedIcon();
                    if (disabledSelectedIcon != null) {
                        disabledSelectedIcon.changeSize((int) (values.getW() * currentScale), (int) (values.getH() * currentScale));
                        button.setDisabledIcon(disabledSelectedIcon);
                    }
                }
            }
        }

        revalidate();
        repaint();
    }

    public void onComponentAdded(Component component) {
        componentList.put(component, new OriginalComponentValues(component));
        scaleComponents();
    }

    public void onComponentRemoved(Component component) {
        componentList.remove(component);
        scaleComponents();
    }

    private static class OriginalComponentValues {
        private final int x, y, w, h;

        public OriginalComponentValues(Component component) {
            x = component.getX();
            y = component.getY();
            w = component.getWidth();
            h = component.getHeight();
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getW() {
            return w;
        }

        public int getH() {
            return h;
        }
    }

    private class DrawPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.drawImage(tempScreen, 0, 0, (int) (panelWidth * currentScale), (int) (panelHeight * currentScale), null);

        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension((int) (panelWidth * currentScale), (int) (panelHeight * currentScale));
        }

        @Override
        public Component add(Component comp) {
            Component addedComp = super.add(comp);

            // Notify or handle the addition of the component
            onComponentAdded(addedComp);

            return addedComp;
        }

        @Override
        public void remove(Component comp) {
            onComponentRemoved(comp);

            super.remove(comp);
        }

        public DrawPanel() {
            setLayout(null);
        }
    }

    public Graphics2D getGraphics2D() {
        return graphics2D;
    }

    public int getPanelWidth() {
        return panelWidth;
    }

    public int getPanelHeight() {
        return panelHeight;
    }

    public void startGameLoop() {
        gameLoop.start();
    }

    public void stopGameLoop() {
        gameLoop.stop();
    }

    public DrawPanel getDrawPanel() {
        return drawPanel;
    }

    public int[] getMousePos() {
        return mousePos;
    }

    public void closeWindow() {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

}
