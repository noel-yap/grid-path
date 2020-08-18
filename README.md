# Grid Path

 Write a function that given the follow information:
 - Square grid of size N
 - Initial starting coordinates
 - A list of steps (Up, Down, Left, Right);

 The function should return
 - The destination coordinates by navigating the grid using the list of steps provided.

 Notes
 - If a step takes the current coordinate out of bounds, then reset that specific dimension to the opposite side of the grid.
 - There may be *multiple* obstacles in the grid, if an obstacle is encountered, return the coordinate of the cell just before the obstacle was encountered.

---

 *Example*
 - 5x5 Grid
 - Starting coordinate (0, 1)
 - Steps: [L, D, D, R]
 <pre>
 ┌───┬───┬───┬───┬───┐
 │ . ← S │   │   │   │
 ├─↓─┼───┼───┼───┼───┤
 │ . │   │   │   │   │
 ├─↓─┼───┼───┼───┼───┤
 │ . → D │   │   │   │
 ├───┼───┼───┼───┼───┤
 │   │   │   │   │   │
 ├───┼───┼───┼───┼───┤
 │   │   │   │   │   │
 └───┴───┴───┴───┴───┘
 </pre>
 > Answer: (2, 1)

---

 *Example*
 - 5x5 Grid
 - Starting coordinate (0, 1)
 - Steps: [L, L, U, U]
 <pre>
 ┌───┬───┬───┬───┬─↑─┐
 ← . ← S │   │   │ . ←
 ├───┼───┼───┼───┼───┤
 │   │   │   │   │   │
 ├───┼───┼───┼───┼───┤
 │   │   │   │   │   │
 ├───┼───┼───┼───┼───┤
 │   │   │   │   │ D │
 ├───┼───┼───┼───┼─↑─┤
 │   │   │   │   │ . │
 └───┴───┴───┴───┴─↑─┘
 </pre>
 Answer: (3, 4)

---

 *Example*
 - 5x5 Grid
 - Starting coordinate (0, 1)
 - Obstacle at (0, 2)
 - Steps: [R, R, D]
 <pre>
 ┌───┬───┬───┬───┬───┐
 │   │ S → X │   │   │
 ├───┼───┼───┼───┼───┤
 │   │   │   │   │   │
 ├───┼───┼───┼───┼───┤
 │   │   │   │   │   │
 ├───┼───┼───┼───┼───┤
 │   │   │   │   │   │
 ├───┼───┼───┼───┼───┤
 │   │   │   │   │   │
 └───┴───┴───┴───┴───┘
 </pre>
 > Answer: (0, 1)
 --------------------------------------


 --------------------------------------
 Write a function that given a rectangular grid, and a collection of available steps,
 an initial starting coordinate, and a destination coordinate;

 determine a possible path required to navigate from the starting coordinate
 to the destination coordinate using a subset of the available steps, each available step can
 only be used once.

 Again, obstacles may be present and will block any potential paths. Boundary conditions should
 be treated in the same way as the previous exercise.

 *Example*
 - 5x5 Grid
 - Starting coordinate (0, 1)
 - Destination coordinate (4, 3)
 - Available Steps: [U, D, U, R, R, D, L, L, L]
 <pre>
 ┌───┬─↑─┬───┬───┬───┐
 │ X │ S │ X │   │   │
 ├───┼───┼───┼───┼───┤
 │ X │ X │ X │   │   │
 ├───┼───┼───┼───┼───┤
 │   │   │   │   │   │
 ├───┼───┼───┼───┼───┤
 │   │ . → . → . │   │
 ├───┼─↑─┼───┼─↓─┼───┤
 │ X │ . │ X │ D │   │
 └───┴───┴───┴───┴───┘
 </pre>
 > Possible answer: [U, U, R, R, D]<br/>
 > Possible answer: [U, U, L, L, L, D]
 --------------------------------------

 peck@squareup.com

## Overview

Grids keep track of the obstacles.
Paths keep track of the list of coordinates that have been visited, the directions to get there, and the available steps.

The general strategy is to start at the starting coordinate and try out each available step.
Each exploration keeps track of valid paths and which steps are still available.
If an exploration hits an obstacle or retraces steps, it's pruned out of the exploration space.
The algorithm records paths leading to the destination and continues to explore all other paths. 
Once there are no more paths to explore, it returns a complete list of paths leading to the destination.

Path.findDirectionsToDestination is the entry point to the final problem. PathTest.shouldFindDirectionsToDestination verifies the given scenario.

PathExploratoryTest.shouldFindDirectionsToDestination exists for manual exploratory testing.

## Running the tests

`./gradlew build`

## Known issues

Exhausted heap space if the solution space is too large.
