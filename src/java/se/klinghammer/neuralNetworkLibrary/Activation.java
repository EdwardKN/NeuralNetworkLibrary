package se.klinghammer.neuralNetworkLibrary;

public enum Activation {
    None {
        @Override
        public double activate(double input) {
            return input;
        }
    },
    Sigmoid {
        @Override
        public double activate(double input) {
            return 1.0 / (1.0 + Math.exp(-input));
        }
    },
    Tanh {
        @Override
        public double activate(double input) {
            return Math.tanh(input);
        }
    },
    ReLU {
        @Override
        public double activate(double input) {
            return Math.max(0, input);
        }
    },
    LeakyReLU {
        @Override
        public double activate(double input) {
            return input >= 0 ? input : 0.01 * input;
        }
    };

    public double activate(double input) {
        throw new UnsupportedOperationException("This activation function does not support single input activation");
    }

    public static Activation getFromString(String activation) {
        for (Activation act : Activation.values()) {
            if (act.name().equalsIgnoreCase(activation)) {
                return act;
            }
        }
        throw new IllegalArgumentException("No activation function found for: " + activation);
    }
}
