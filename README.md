# 03 Snowflake

### By Nick Brown

## Usage

Run the file with

    javac Snowflake.java

    java Snowflake

Run with command line argument "d" to enable debug mode.

Use the slider at the top of the screen to change the order of the snowflake, and the plus and minus buttons to increment or decrement by 1.

Use the slider at the bottom of the screen to increase or decrease the zoom, and the "Reset Zoom" button to revert to the default zoom.

## Known Issues

- The snowflake will disappear when viewed from certain positions when sufficiently zoomed in. This is due to a fault in the culling methods and a solution is actively being investigated.

## Dev Log

### Zoom

The zoom function was originally just a flat multiplier applied to the coordinates of each point of the snowflake, that could be increased or decreased by scrolling the mouse. However, I observed that the zoom speed would decrease as you zoomed further into the snowflake, or increase as you zoom out. I realized that setting the multiplier to be exponential rather than linear would mitigate this issue, so I set the multiplier to be **e^zoom**. This stayed for about a week until one day when I was thinking about how the lines were divided into three equal segments for each additional order, which made me suddenly realize that if I instead set the multiplier to **3^zoom** it would maintain a mathematically perfect and consistent zoom speed. So I did that.

### Dragging

By clicking and dragging the mouse, the snowflake will be moved along with it, allowing for viewing it in different positions. 

### Optimization & Culling

When drawing the snowflake, each line is run through a series of tests to calculate whether or not it will be visible; if it is not visible, it and its children will not be drawn or calculated. Each line is subjected to the following tests, in order:

- Either the starting or ending points are within the viewing area

- The line intersects the bounding box around the viewing area

If these tests fail, a triangle is created surrounding the line and its future immediate children. 

![tringle](https://i.imgur.com/dvXf4ID.png)

The two equal lines of this new isosceles triangle are then subjected to the above two tests. If this also fails, one last test is considered:

- The point in the center of the screen resides within the triangle

If all tests fail, the line is considered invisible and is not drawn, and its children are not calculated. This process greatly increases performance at high levels. However, the final test concerning the point in the triangle is inconsistent with sufficiently large triangles wherein the precision of the equations breaks down and produces false negatives, causing the snowflake to disappear.
