package se.test.neuralNetwork.src.visual;

public class PositionedNode {
    private final int x, y, type;

    public PositionedNode(int x, int y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getType() {
        return type;
    }
}