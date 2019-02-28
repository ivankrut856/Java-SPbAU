package fr.ladybug.team;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.Arrays;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CalculatorTest {

    Stack<String> mockStack;
    Calculator calculator;

    @BeforeEach
    public void initializeMock() {
        mockStack = mock(Stack.class);
        calculator = new Calculator(mockStack);
    }

    @Test
    public void testEvaluateNothing() {
        when(mockStack.size()).thenReturn(1);
        when(mockStack.pop()).thenReturn("5");
        assertEquals(5, calculator.evaluate("5"));

        InOrder inOrder = inOrder(mockStack);
        inOrder.verify(mockStack).push("5");
        inOrder.verify(mockStack).size();
        inOrder.verify(mockStack).pop();
    }

    @Test
    public void testEvaluateAddition() {
        when(mockStack.size()).thenReturn(2, 1);
        when(mockStack.pop()).thenReturn("1", "4", "5");
        assertEquals(5, calculator.evaluate("4 1 +"));

        InOrder inOrder = inOrder(mockStack);
        inOrder.verify(mockStack).push("4");
        inOrder.verify(mockStack).push("1");
        inOrder.verify(mockStack).size();
        inOrder.verify(mockStack, times(2)).pop();
        inOrder.verify(mockStack).push("5");
        inOrder.verify(mockStack).size();
        inOrder.verify(mockStack).pop();
    }

    @Test
    public void testEvaluateSubtraction() {
        when(mockStack.size()).thenReturn(2, 1);
        when(mockStack.pop()).thenReturn("1", "4", "3");
        assertEquals(3, calculator.evaluate("4 1 -"));

        InOrder inOrder = inOrder(mockStack);
        inOrder.verify(mockStack).push("4");
        inOrder.verify(mockStack).push("1");
        inOrder.verify(mockStack).size();
        inOrder.verify(mockStack, times(2)).pop();
        inOrder.verify(mockStack).push("3");
        inOrder.verify(mockStack).size();
        inOrder.verify(mockStack).pop();
    }

    @Test
    public void testEvaluateMultiplication() {
        when(mockStack.size()).thenReturn(2, 1);
        when(mockStack.pop()).thenReturn("1", "4", "4");
        assertEquals(4, calculator.evaluate("4 1 *"));

        InOrder inOrder = inOrder(mockStack);
        inOrder.verify(mockStack).push("4");
        inOrder.verify(mockStack).push("1");
        inOrder.verify(mockStack).size();
        inOrder.verify(mockStack, times(2)).pop();
        inOrder.verify(mockStack).push("4");
        inOrder.verify(mockStack).size();
        inOrder.verify(mockStack).pop();
    }

    @Test
    public void testEvaluateDivision() {
        when(mockStack.size()).thenReturn(2, 1);
        when(mockStack.pop()).thenReturn("1", "4", "4");
        assertEquals(4, calculator.evaluate("4 1 /"));

        InOrder inOrder = inOrder(mockStack);
        inOrder.verify(mockStack).push("4");
        inOrder.verify(mockStack).push("1");
        inOrder.verify(mockStack).size();
        inOrder.verify(mockStack, times(2)).pop();
        inOrder.verify(mockStack).push("4");
        inOrder.verify(mockStack).size();
        inOrder.verify(mockStack).pop();
    }

    @Test
    public void testArithmeticException() {
        calculator = new Calculator(new Stack<>());
        assertThrows(ArithmeticException.class, ()-> {
            calculator.evaluate("4 1 + 6 6 - /");
        });
    }

    @Test
    public void testSyntaxError() {
        calculator = new Calculator(new Stack<>());
        assertThrows(IllegalStateException.class, () -> {
           calculator.evaluate("4 + 1");
        });
        assertThrows(IllegalStateException.class, () -> {
            calculator.evaluate("5 3 1 *");
        });
    }

}