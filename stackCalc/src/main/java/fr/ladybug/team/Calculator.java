package fr.ladybug.team;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Stack;

public class Calculator {
    Stack<String> container;

    public Calculator(Stack<String> container) {
        this.container = container;
    }

    public int evaluate(String expression) {
        String[] tokens = expression.split(" ");
        for (var token : tokens) {
            try {
                Integer.parseInt(token);
                container.push(token);
            }
            catch (NumberFormatException e) {
                switch (token) {
                    case "+": {
                        if (container.size() < 2) {
                            throw new IllegalStateException("Syntax error");
                        }
                        int secondOperand = Integer.parseInt(container.pop());
                        int firstOperand = Integer.parseInt(container.pop());
                        container.push(String.valueOf(firstOperand + secondOperand));
                        break;
                    }
                    case "-": {
                        if (container.size() < 2) {
                            throw new IllegalStateException("Syntax error");
                        }
                        int secondOperand = Integer.parseInt(container.pop());
                        int firstOperand = Integer.parseInt(container.pop());
                        container.push(String.valueOf(firstOperand - secondOperand));
                        break;
                    }
                    case "/": {
                        if (container.size() < 2) {
                            throw new IllegalStateException("Syntax error");
                        }
                        int secondOperand = Integer.parseInt(container.pop());
                        int firstOperand = Integer.parseInt(container.pop());
                        if (secondOperand == 0)
                            throw new ArithmeticException("Division by zero");
                        container.push(String.valueOf(firstOperand / secondOperand));
                        break;
                    }
                    case "*": {
                        if (container.size() < 2) {
                            throw new IllegalStateException("Syntax error");
                        }
                        int secondOperand = Integer.parseInt(container.pop());
                        int firstOperand = Integer.parseInt(container.pop());
                        container.push(String.valueOf(firstOperand * secondOperand));
                        break;

                    }
                    default: {
                        throw new IllegalArgumentException();
                    }
                }
            }
        }
        if (container.size() != 1)
            throw new IllegalStateException("Syntax error");
        return Integer.parseInt(container.pop());
    }
}
