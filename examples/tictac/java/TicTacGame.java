import se.klinghammer.neuralNetworkLibrary.Individual;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class TicTacGame extends JPanel {
    private boolean xTurn;
    private CellState[] states = new CellState[81];
    private CellState[] big = new CellState[9];
    private JButton[] buttons = new JButton[81];
    private boolean[] disabled = new boolean[81];
    private int buttonSize = 50;
    private boolean rendering;

    private NetworkAdapter networkAdapterNought;
    private NetworkAdapter networkAdapterCross;
    private Propagater propagaterNought;
    private Propagater propagaterCross;

    private Runnable taskOnFinish;

    private boolean running;

    private CellState winner;

    public enum CellState {
        EMPTY(""),
        NOUGHT("O"),
        CROSS("X");

        private final String text;

        CellState(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    public void setSamples(Individual[] samples) {
        propagaterNought = new Propagater(samples[0], networkAdapterNought);
        propagaterCross = new Propagater(samples[1], networkAdapterCross);
    }

    public void setSample(Individual sample) {
        if (Math.random() > 0.5) {
            propagaterNought = new Propagater(sample, networkAdapterNought);
        } else {
            propagaterCross = new Propagater(sample, networkAdapterCross);
        }
    }

    public void setTaskOnFinish(Runnable taskOnFinish) {
        this.taskOnFinish = taskOnFinish;
    }

    public void run(int sleepTime) {
        running = true;

        while (running) {
            if (sleepTime != 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if (xTurn && propagaterCross != null) {
                propagaterCross.propagate();
                double[] propagation = propagaterCross.getPropagation();

                for (int i = 0; i < disabled.length; i++) {
                    propagation[i] = disabled[i] ? -1 : propagation[i];
                }

                if (Arrays.stream(propagation).allMatch((e) -> e == -1)) {
                    break;
                }

                int maxIndex = getMaxIndex(propagation);

                updateSquare(maxIndex);
            } else if (propagaterNought != null) {
                propagaterNought.propagate();
                double[] propagation = propagaterNought.getPropagation();

                for (int i = 0; i < disabled.length; i++) {
                    propagation[i] = disabled[i] ? -1 : propagation[i];
                }

                if (Arrays.stream(propagation).allMatch((e) -> e == -1)) {
                    break;
                }
                int maxIndex = getMaxIndex(propagation);

                updateSquare(maxIndex);
            }
        }

        propagaterNought.addFitness(winner == CellState.NOUGHT ? 1 : 0);
        propagaterCross.addFitness(winner == CellState.CROSS ? 1 : 0);
        if (taskOnFinish != null) {
            taskOnFinish.run();
        }
    }

    public int getMaxIndex(double[] array) {
        int maxIndex = 0;  // Assume the first element is the max initially
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIndex]) {
                maxIndex = i;  // Update max index when a larger value is found
            }
        }
        return maxIndex;
    }

    public TicTacGame(boolean rendering) {
        this.rendering = rendering;
        setLayout(null);
        setPreferredSize(new Dimension((buttonSize + 2) * 9, (buttonSize + 2) * 9));
        setSize(new Dimension((buttonSize + 2) * 9, (buttonSize + 2) * 9));

        networkAdapterNought = new NetworkAdapter();
        networkAdapterCross = new NetworkAdapter();

        Arrays.fill(big, CellState.EMPTY);

        for (int i = 0; i < states.length; i++) {
            states[i] = CellState.EMPTY;

            if (rendering) {
                buttons[i] = new JButton("");
                buttons[i].setBounds((buttonSize + 2) * (i % 9), (buttonSize + 2) * (i / 9), buttonSize, buttonSize);

                int finalI = i;
                buttons[i].addActionListener((actionEvent) -> {
                    updateSquare(finalI);
                });
                add(buttons[i]);
            }
        }
    }

    public void updateSquare(int index) {
        states[index] = xTurn ? CellState.CROSS : CellState.NOUGHT;

        networkAdapterNought.setCellValue(index, xTurn ? -1 : 1);
        networkAdapterCross.setCellValue(index, xTurn ? 1 : -1);

        if (rendering) {
            buttons[index].setText(xTurn ? CellState.CROSS.getText() : CellState.NOUGHT.getText());
            buttons[index].setEnabled(false);
        }
        disabled[index] = true;

        CellState[] currentGame = new CellState[9];
        int x = ((int) ((index % 9) / 3.0));
        int y = ((int) ((index) / 27.0));

        for (int i = 0; i < 9; i++) {
            currentGame[i] = states[i % 3 + x * 3 + y * 27 + (int) (i / 3.0) * 9];
        }

        boolean hasWon = hasWon(xTurn ? CellState.CROSS : CellState.NOUGHT, currentGame);

        if (hasWon) {
            int index2 = x * 3 + y * 27;
            for (int i = 0; i < 9; i++) {
                if (rendering) {
                    buttons[i % 3 + index2 + (int) (i / 3.0) * 9].setVisible(false);
                }
                networkAdapterNought.setCellValue(i % 3 + index2 + (int) (i / 3.0) * 9, xTurn ? -1 : 1);
                networkAdapterCross.setCellValue(i % 3 + index2 + (int) (i / 3.0) * 9, xTurn ? 1 : -1);
            }
            if (rendering) {
                buttons[index2].setVisible(true);
                buttons[index2].setBounds((buttonSize + 2) * (index2 % 9), (buttonSize + 2) * (index2 / 9), buttonSize * 3 + 6, buttonSize * 3 + 6);
                buttons[index2].setText(xTurn ? CellState.CROSS.getText() : CellState.NOUGHT.getText());
            }
            states[index2] = xTurn ? CellState.CROSS : CellState.NOUGHT;
            big[x + y * 3] = xTurn ? CellState.CROSS : CellState.NOUGHT;

        }


        int xNewCell = index % 3;
        int yNewCell = ((int) (index / 9.0)) % 3;

        boolean hasWonBig = hasWon(xTurn ? CellState.CROSS : CellState.NOUGHT, big);

        if (hasWonBig || Arrays.stream(states).allMatch(i -> i != CellState.EMPTY)) {
            winner = xTurn ? CellState.CROSS : CellState.NOUGHT;
            running = false;
        }


        if (big[xNewCell + 3 * yNewCell] == CellState.EMPTY) {
            Arrays.fill(disabled, true);
            if (rendering) {
                for (JButton button : buttons) {
                    button.setEnabled(false);
                }
            }
            for (int j = 0; j < 9; j++) {
                int index2 = xNewCell * 3 + j % 3 + yNewCell * 27 + ((int) (j / 3.0)) * 9;
                if (states[index2] == CellState.EMPTY) {
                    disabled[index2] = false;
                    if (rendering) {
                        buttons[index2].setEnabled(true);
                    }
                }
            }
        } else {
            for (int i = 0; i < buttons.length; i++) {
                if (states[i] == CellState.EMPTY) {
                    disabled[i] = false;
                    if (rendering) {
                        buttons[i].setEnabled(true);
                    }
                }
            }
        }

        xTurn = !xTurn;
    }

    private boolean hasWon(CellState player, CellState[] board) {
        int[][] winningCombinations = {
                {0, 1, 2},
                {3, 4, 5},
                {6, 7, 8},
                {0, 3, 6},
                {1, 4, 7},
                {2, 5, 8},
                {0, 4, 8},
                {2, 4, 6}
        };

        // Check each winning combination
        for (int[] combination : winningCombinations) {
            if (board[combination[0]] == player &&
                    board[combination[1]] == player &&
                    board[combination[2]] == player) {
                return true;
            }
        }
        return false;
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setStroke(new BasicStroke(4));

        for (int i = 0; i < 3; i++) {
            g2d.drawLine((buttonSize + 2) * i * 3, 0, (buttonSize + 2) * i * 3, (buttonSize + 2) * 9);
        }
        for (int i = 0; i < 3; i++) {
            g2d.drawLine(0, (buttonSize + 2) * i * 3, (buttonSize + 2) * 9, (buttonSize + 2) * i * 3);
        }

        g2d.setStroke(new BasicStroke(1));

    }
}
