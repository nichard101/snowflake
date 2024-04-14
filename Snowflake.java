import java.awt.*;
import javax.swing.*;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.lang.Math;
import java.util.Arrays;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.math.BigDecimal;
import java.util.ArrayList;

public class Snowflake {
    static int order;
    static JLabel label;
    static Line L;
    static JSlider orderSlider;
    static JSlider zoomSlider;
    static double zoom;

    public static void main(String[] args){
        L = new Line(3, 1200, 700);

        for(int i = 0; i < args.length; i++){
            System.out.println(args[i]);
            if(args[i].equals("d")){
                L.Debug();
            }
        }

        orderSlider = new JSlider(JSlider.HORIZONTAL, 1, 500, L.GetOrder());
        orderSlider.setMajorTickSpacing(1);

        order = orderSlider.getValue();

        zoomSlider = new JSlider(JSlider.HORIZONTAL, -2000, L.GetOrder()*1000, 1000);
        zoomSlider.setMajorTickSpacing(1);

        // Label that displays the current order
        label = new JLabel("Order " + order);

        // Buttons to change the order by 1 in either direction
        JButton plusButton = new JButton("+");
        JButton minusButton = new JButton("-");

        plusButton.addActionListener(new ActionListener(){ // Increases the order by 1 when this button is clicked
            public void actionPerformed(ActionEvent e){
                Update(order+1);
            }
        });

        minusButton.addActionListener(new ActionListener(){ // Decreases the order by 1 when this button is clicked
            public void actionPerformed(ActionEvent e){
                Update(order-1);
            }
        });

        JButton resetZoomButton = new JButton("Reset Zoom");

        resetZoomButton.addActionListener(new ActionListener(){ // Resets the zoom to 0.25 once clicked
            public void actionPerformed(ActionEvent e){
                L.ResetZoom();
                zoomSlider.setValue((int)(L.zoom * 1000));
                zoom = L.zoom;
                L.repaint();
            }
        });
        
        orderSlider.addChangeListener(new ChangeListener(){ // Updates the order when the slider is moved
            public void stateChanged(ChangeEvent e){
                Update(orderSlider.getValue());
            }
        });

        zoomSlider.addChangeListener(new ChangeListener(){ // Updates the level of zoom when the slider is moved
            public void stateChanged(ChangeEvent e){
                L.ZoomSlider(zoomSlider.getValue()/1000.0);
                L.repaint();
            }
        });

        JPanel sidePanel = new JPanel();
        sidePanel.add(resetZoomButton);
        sidePanel.add(zoomSlider);

        JPanel topPanel = new JPanel();
        topPanel.add(label);
        topPanel.add(orderSlider);
        topPanel.add(minusButton);
        topPanel.add(plusButton);

        JFrame window = new JFrame("Snowflake");

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.getContentPane().setLayout(new BoxLayout(window.getContentPane(), BoxLayout.Y_AXIS));
        window.setPreferredSize(new Dimension(1200, 700));

        window.addComponentListener(new ComponentAdapter(){ // Adjusts the snowflake size relative to the window size
            public void componentResized(ComponentEvent e){
                L.setHeight((int)window.getContentPane().getHeight()); 
                L.setWidth((int)window.getContentPane().getWidth());
                L.repaint();
            }
        });

        window.addMouseWheelListener(new MouseWheelListener(){ // Zooms in and out using the mouse wheel
            public void mouseWheelMoved(MouseWheelEvent e){
                L.Zoom(e.getPreciseWheelRotation()/15);
                zoomSlider.setValue((int)(L.zoom*1000));
                L.repaint();
            }
        });

        window.addMouseMotionListener(new MouseMotionListener(){ // Drag the snowflake around with the mouse
            public void mouseMoved(MouseEvent e){
                
            }
            public void mouseDragged(MouseEvent e){
                PointNew p = L.getPoint();
                double x = e.getX() - p.x;
                double y = e.getY() - p.y;
                L.Drag(x, y);
                L.SetPoint(new PointNew(e.getPoint()));
                L.repaint();
            }
        });
        
        window.addMouseListener(new MouseAdapter(){ // Get the point wherever the mouse is clicked
            public void mousePressed(MouseEvent e){
                L.SetPoint(new PointNew(e.getX(), e.getY()));
                L.repaint();
            }
        });

        window.getContentPane().add(topPanel);
        window.getContentPane().add(L);
        window.getContentPane().add(sidePanel);

        window.pack();
        window.setVisible(true);
    }

    /**
     * A method to update the current order when it is altered somewhere
     * @param o , the new order
     */
    public static void Update(int o){
        if(o >= 1){
            order = o;
            label.setText("Order " + order);
            L.SetOrder(order);
            orderSlider.setValue(order);
            zoomSlider.setMaximum(order*10000);
            L.repaint();
        }
    }
}

class Line extends JPanel {

    int order;

    double size;

    // The dimensions of the canvas
    int width;
    int height;

    // The center of the shape
    double xOffset = 0;
    double yOffset = 0;

    // The level of zoom
    double zoom = 0.1;

    // The center of the canvas
    double xCenter;
    double yCenter;

    // The position of the mouse
    double mouseX;
    double mouseY;

    double centerX, centerY;

    int minX = -10;
    int minY = -10;

    Graphics g;

    PointNew topLeft = new PointNew(minX,minY);
    PointNew bottomLeft = new PointNew(minX, height-minY);
    PointNew topRight = new PointNew(width-minX, minY);
    PointNew bottomRight = new PointNew(width-minX, height-minY);

    int minOrder;
    int counter;
    int numByPoint;
    int numByInts;
    int numByTrianglePoint;
    int numByTriangleLine;
    int numByTriangleEnvelop;

    boolean debug = false;

    public void Debug(){
        minX = 500;
        minY = 300;
        debug = true;
    }

    public void paintComponent(Graphics gr){
        this.g = gr;
        super.paintComponent(g);
        g.setColor(Color.red);
        g.drawOval((int)xCenter, (int)yCenter, 5, 5);
        Line2D.Double lineToCenter = new Line2D.Double(centerX, centerY, xCenter, yCenter);
        if(debug){
            for(int i = 0; i < height; i+=100){
                DrawLine(g, 0, i, width, i);
            }
            for(int i = 0; i < width; i+=100){
                DrawLine(g, i, 0, i, height);
            }
            g.setColor(Color.blue);
            DrawLine(g, topLeft, bottomLeft); // top left to bottom left
            DrawLine(g, topLeft, topRight); // top left to top right
            DrawLine(g, bottomLeft, bottomRight); // bottom left to bottom right
            DrawLine(g, bottomRight,topRight); // bottom right to top right
            g.setColor(Color.pink);
            DrawLine(g, lineToCenter);
            System.out.println("Length: " + Length(lineToCenter));
            System.out.println(lineToCenter.getX1() + " " + lineToCenter.getX2() + " " + lineToCenter.getY1() + " " + lineToCenter.getY2());
            System.out.println((lineToCenter.getY2() - lineToCenter.getY1())/(lineToCenter.getX2() - lineToCenter.getX1()));
        }
      
        g.setColor(Color.black);  
        counter = 0;
        numByInts = 0;
        numByPoint = 0;
        numByTriangleLine = 0;
        numByTrianglePoint = 0;
        numByTriangleEnvelop = 0;


        double[] p1D = new double[2];
        double[] p2D = new double[2];
        double[] p3D = new double[2];

        centerX = xCenter + Math.pow(3,zoom)*xOffset;
        centerY = yCenter + Math.pow(3,zoom)*yOffset;

        p1D[0] = xCenter + Math.pow(3,zoom)*(-size/2 + xOffset);
        p1D[1] = yCenter + Math.pow(3,zoom)*((3*size/8)*Math.cos(Math.PI/6) + yOffset);

        p2D[0] = xCenter + Math.pow(3,zoom)*(size/2 + xOffset);
        p2D[1] = yCenter + Math.pow(3,zoom)*((3*size/8)*Math.cos(Math.PI/6) + yOffset);
        
        p3D[0] = xCenter + Math.pow(3,zoom)*(xOffset);
        p3D[1] = yCenter + Math.pow(3,zoom)*(-(5*size/8)*Math.cos(Math.PI/6) + yOffset);

        PointNew p1 = new PointNew(p1D[0], p1D[1]);
        PointNew p2 = new PointNew(p2D[0], p2D[1]);
        PointNew p3 = new PointNew(p3D[0], p3D[1]);
        PointNew[] pList = {p1,p2,p3,p1};

        DrawPoints(g, order, pList);
        
        if(debug){
            System.out.println("Lines: " + counter);
            System.out.println("By point: " + numByPoint + "\nBy line: " + numByInts + "\nBy triangle: \n\tPoint:" + numByTrianglePoint + "\n\tLine: " + numByTriangleLine + "\n\tEnveloped: " + numByTriangleEnvelop);
            System.out.println("Total: " + (numByPoint + numByInts + numByTrianglePoint + numByTriangleLine + numByTriangleEnvelop));
            System.out.println("");
        }
        
    }

    void DrawPoints(Graphics g, int o, PointNew[] pointList){
        for(int i = 1; i < pointList.length; i++){
            if(VisibilityCheck(pointList[i-1], pointList[i])){
                koch(g, o, pointList[i-1], pointList[i]);
            }
        }
    }

    void koch(Graphics g, int o, PointNew p1, PointNew p2){
        koch(g, o, p1.x, p1.y, p2.x, p2.y);
    }

    /**
     * A method to recursively generate smaller triangles
     * @param g
     * @param o The order (or level) of triangle to generate
     * @param x1
     * @param y1
     * @param x5
     * @param y5
     */
    void koch(Graphics g, int o, double x1, double y1, double x5, double y5){
        double x2, y2, x3, y3, x4, y4, dx, dy;

        dx = x5 - x1;
        dy = y5 - y1;

        x2 = x1 + dx/3;
        y2 = y1 + dy/3;

        x3 = (0.5 * (x1 + x5) + Math.sqrt(3) * (y1-y5)/6);
        y3 = (0.5 * (y1 + y5) + Math.sqrt(3) * (x5-x1)/6);

        x4 = x1 + 2*dx/3;
        y4 = y1 + 2*dy/3;
        if(debug){
            g.setColor(Color.green);
            DrawLine(g, x1, y1, x5, y5);
            DrawLine(g, x1, y1, x3, y3);
            DrawLine(g, x3, y3, x5, y5);
        }
            
        g.setColor(Color.black);

        PointNew p1 = new PointNew(x1, y1);
        PointNew p2 = new PointNew(x2, y2);
        PointNew p3 = new PointNew(x3, y3);
        PointNew p4 = new PointNew(x4, y4);
        PointNew p5 = new PointNew(x5, y5);
        PointNew[] pList = {p1,p2,p3,p4,p5};

        if(o > 1 && Length(x1, y1, x5, y5) >= 3){ // If the order is higher than 1 and the length of the current triangle is less than 3 pixels
            // Draws four lines forming one segment of this level of Koch's curve
            DrawPoints(g, o-1, pList);
        } else { // If the final order or minimum triangle size has been reached, draw the line
            counter ++;
            DrawLine(g, p1, p5);
        }
    }

    /**
     * A method to draw a line between two vector points
     * @param g
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    void DrawLine(Graphics g, double x1, double y1, double x2, double y2){
        Graphics2D g2 = (Graphics2D) g;
        Line2D.Double l = new Line2D.Double(x1,y1,x2,y2);
        DrawLine(g2,l);
    }

    void DrawLine(Graphics2D g, Line2D.Double in){
        g.draw(in);
    }

    void DrawLine(Graphics g, Line2D.Double in){
        Graphics2D g2 = (Graphics2D)g;
        g2.draw(in);
    }

    void DrawLine(Graphics g, PointNew p1, PointNew p2){
        DrawLine(g, p1.x, p1.y, p2.x, p2.y);
    }

    boolean VisibilityCheck(PointNew p1, PointNew p2){
        if(IsPointVisible(p1) || IsPointVisible(p2)){
            numByPoint++;
            return true;
        }
        if(IsLineVisible(p1, p2)){
            numByInts++;
            return true;
        } 
        if(IntersectTriangle(p1, p2)){
            return true;
        }
        return false;
    }

    /**
     * A method to detect whether a PointNew object is visible or not
     * @param p
     * @return true if the PointNew is visible, false if it is not
     */
    boolean IsPointVisible(PointNew p){
        if((p.x >= minX && p.x <= width - minX) && (p.y >= minY && p.y <= height - minY)){
            return true;
        } else {
            return false;
        }
    }

    /**
     * A method to detect whether a line between two PointNew objects is visible or not
     * @param p1
     * @param p2
     * @return true if the line is visible at any point, false otherwise
     */
    boolean IsLineVisible(PointNew p1, PointNew p2){
        if(NewLineIntersect(p1, p2, bottomRight, topRight)){
            return true;
        }
        if(NewLineIntersect(p1, p2, topLeft, bottomLeft)){
            return true;
        }
        if(NewLineIntersect(p1, p2, topLeft, topRight)){
            return true;
        }
        if(NewLineIntersect(p1, p2, bottomLeft, bottomRight)){
            return true;
        }
        return false;
    }

    boolean IntersectTriangle(PointNew p2, PointNew p1){
        PointNew p3 = new PointNew((0.5 * (p1.x + p2.x) + Math.sqrt(3) * (p1.y-p2.y)/6), (0.5 * (p1.y + p2.y) + Math.sqrt(3) * (p2.x-p1.x)/6));
        PointNew p4 = new PointNew((0.5 * (p2.x + p1.x) + Math.sqrt(3) * (p2.y-p1.y)/6), (0.5 * (p2.y + p1.y) + Math.sqrt(3) * (p1.x-p2.x)/6));
        if(IsPointVisible(p3) || IsPointVisible(p4)){
            numByTrianglePoint++;
            return true;
        }
        
        if(IsLineVisible(p1, p3) || IsLineVisible(p2, p3) || IsLineVisible(p1, p4) || IsLineVisible(p2, p4)){ // if either of the new lines of the triangle are visible
            numByTriangleLine++;
            return true;
        } else {
            PointNew[] pList1 = {p1, p2, p3};
            PointNew[] pList2 = {p1, p2, p4};
            PointNew p = new PointNew(xCenter, yCenter);
            PointNew[] corners = {topLeft, topRight, bottomLeft, bottomRight, p};
            for(PointNew pN : corners){
                if(IsPointInsideTriangle(pN, pList1) || IsPointInsideTriangle(pN, pList2)){ // if the triangle completely encompasses the view area
                    numByTriangleEnvelop++;
                    return true;
                }
            }
        }
        return false;
    }

    boolean IsPointInsideTriangle(PointNew p, PointNew[] t){
        PointNew a = new PointNew(centerX, centerY);
        PointNew[] line = {a,p};

        int count = CountIntersections(t, line);
        // if(count != 0){
        //     System.out.println("Intersections: " + count);
        // }
        return count==1;
    }

    int CountIntersections(PointNew[] triangle, PointNew[] line){
        int count = 0;

        for(int i = 0; i < triangle.length-1; i++){
            int j = i+1;
            PointNew b;
            PointNew a = triangle[i];
            if(i==triangle.length-1){
                b = triangle[0];
            } else {  
                b = triangle[j];
            }
            if(Line2D.linesIntersect(line[0].x, line[0].y, line[1].x, line[1].y, a.x, a.y, b.x, b.y)){
                count += 1;
            }
        }

        return count;
    }

    boolean NewLineIntersect(PointNew p1, PointNew p2, PointNew p3, PointNew p4){
        double x1 = p1.x;
        double x2 = p2.x;
        double x3 = p3.x;
        double x4 = p4.x;
        double y1 = p1.y;
        double y2 = p2.y;
        double y3 = p3.y;
        double y4 = p4.y;

        return Line2D.linesIntersect(x1, y1, x2, y2, x3, y3, x4, y4);
    }

    boolean PointOnLine(PointNew p, PointNew A, PointNew B){
        double L1 = Length(A, p);
        double L2 = Length(B, p);
        double L3 = Length(A, B);
        if(L1 + L2 >= 0.8*L3 && L1 + L2 <= L3/0.8){
            return true;
        }
        return false;
    }

    /**
     * A method to calculate the length of a line between two coordinates
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return The length of the line
     */
    double Length(double x1, double y1, double x2, double y2){
        return Math.sqrt(Math.pow((x2-x1), 2) + Math.pow((y2-y1), 2));
    }

    double Length(Line2D.Double l){
        return Length(l.getX1(), l.getY1(),l.getX2(),l.getY2());
    }

    /**
     * A method to calculate the length of a line between two PointNew objects
     * @param p1
     * @param p2
     * @return The length of the line
     */
    double Length(PointNew p1, PointNew p2){
        return Length(p1.x, p1.y, p2.x, p2.y);
    }

    public Line(int o){
        this(o, 500);
    }

    public Line(int o, int w){
        this(o, w, w);
    }

    public Line(int o, int w, int h){
        order = o;
        setWidth(w);
        setHeight(h);
        setPreferredSize(new Dimension(width, height));
        setSize();
    }

    public void setHeight(int h){
        height = h;
        yCenter = height/2;
        setSize();
    }

    public void setWidth(int w){
        width = w;
        xCenter = width/2;
        setSize();
    }

    public void setSize(){
        setPreferredSize(new Dimension(width, height));
        if(width > height){
            size = 0.5 * height * Math.pow(3,zoom);
        } else {
            size = 0.5 * width * Math.pow(3,zoom);
        }
        topLeft = new PointNew(minX,minY);
        bottomLeft = new PointNew(minX, height-minY);
        topRight = new PointNew(width-minX, minY);
        bottomRight = new PointNew(width-minX, height-minY);
    }

    public void Zoom(double z){
        zoom -= z;
        if(zoom < -2){
            zoom = -2;
        }
    }

    public void ZoomSlider(double z){
        zoom = z;
    }

    public void Drag(double x, double y){
        xOffset += (x/Math.pow(3,zoom));
        yOffset += (y/Math.pow(3,zoom));
    }

    /**
     * A method to store the location of the mouse
     * @param p , a PointNew corresponding to the cursor's location
     */
    public void SetPoint(PointNew p){
        mouseX = p.x;
        mouseY = p.y;
    }

    public PointNew getPoint(){
        return new PointNew(mouseX, mouseY);
    }

    public PointNew GetSize(){
        return new PointNew(width, height);
    }

    public void SetOrder(int o){
        order = o;
    }

    public int GetOrder(){
        return order;
    }

    public void ResetZoom(){
        zoom = 0.1;
        setSize();
    }
}

class PointNew {
    public double x = 0;
    public double y = 0;

    public PointNew(){}

    public PointNew(double x, double y){
        this.x = x;
        this.y = y;
    }

    public PointNew(Point p){
        this.x = (double)p.x;
        this.y = (double)p.y;
    }
}