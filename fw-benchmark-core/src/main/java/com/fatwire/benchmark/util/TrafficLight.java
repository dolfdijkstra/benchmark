/*
 * RatRace.java
 *
 * Created on March 25, 2002, 12:26 PM
 */

package com.fatwire.benchmark.util;

/**
 *
 * @author  Dolf Dijkstra
 * @version
 */

public class TrafficLight {
    enum Color {
        RED, GREEN
    };

    private Color color = Color.RED;

    public synchronized void waitForGreenLight() {
        while (color == Color.RED) {
            try {
                this.wait();
            } catch (java.lang.InterruptedException e) {

            }
        }
        this.notifyAll();

    }

    /** Getter for property color.
     * @return Value of property color.
     */
    public Color getColor() {
        return color;
    }

    /** Setter for property color.
     * @param color New value of property color.
     */
    private synchronized void setColor(Color color) {
        this.color = color;
        this.notifyAll();
    }
    
    public void turnRed(){
        setColor(Color.RED);
    }
    public void turnGreen(){
        setColor(Color.GREEN);
    }

}
