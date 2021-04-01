package com.algotrading.base.examples;


import com.algotrading.base.core.window.Window;

/**
 * Тестирование класса Window.
 */
class WindowTestCase {

    public static void main(final String[] args) {
        final Window<Integer> window = new Window<>(5);
        window.add(0);
        System.out.println(window.get(0) + ", " + window.isFull());
        window.add(1);
        System.out.println(window.get(0) + ", " + window.isFull());

        testIterator(window);

        window.add(2);
        System.out.println(window.get(0) + ", " + window.isFull());
        window.add(3);
        System.out.println(window.get(0) + ", " + window.isFull());
        window.add(4);
        System.out.println(window.get(0) + ", " + window.isFull());
        window.add(5);
        System.out.println(window.get(0) + ", " + window.isFull());
        window.add(6);
        System.out.println(window.get(0) + ", " + window.isFull());
        window.add(7);
        System.out.println(window.get(0) + ", " + window.isFull());
        System.out.println(window.get(-1));
        System.out.println(window.get(-2));
        System.out.println(window.get(-3));
        System.out.println(window.get(-4));

        testIterator(window);

        window.clear();
        testIterator(window);

        window.add(8);
        System.out.println(window.get(0) + ", " + window.isFull());
        window.add(9);
        System.out.println(window.get(0) + ", " + window.isFull());
        window.add(10);
        System.out.println(window.get(0) + ", " + window.isFull());
        System.out.println(window.get(-1));
        System.out.println(window.get(-2));

        testIterator(window);

        System.out.println("ArrayIndexOutOfBoundsException is going to show up...");
        System.out.println(window.get(-3));
    }

    private static void testIterator(final Window<Integer> window) {
        System.out.println();
        System.out.println("Iterating " + window.size() + " elements:");
        for (final int i : window) {
            System.out.println(i);
        }
        System.out.println("Done");
        System.out.println();
    }
}
