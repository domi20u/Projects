**Comparing PD, PID and adaptive controllers for three joints of the UR-10 robot arm.**

*Running on Ubuntu 16-04 with ROS kinetic and MATLAB.*

The first three DoFs are controlled by different controllers to move along defined trajectories within 30s. The first six seconds, the robot moves to a non-singular position always using a controller in joint space. The next six seconds it moves to a goal position, holds it for a few seconds and then performing two rythmic trajectories with varying frequencies.

//Selecting the controller-mode in the [TrajGen](https://github.com/domi20u/Projects/blob/master/Robot%20Control/Controller_Comparison/src/Session6_Template/TrajGen.m)-file, then runnning the [Simulink model](https://github.com/domi20u/Projects/blob/master/Robot%20Control/Controller_Comparison/src/Session6_Template/DSimulator_robot3GDL.mdl). Modes and comments can be found in the [PDF](https://github.com/domi20u/Projects/blob/master/Robot%20Control/Controller-Comparison.pdf).

![Adaptive-Controller](https://github.com/domi20u/Projects/blob/master/Robot%20Control/80_Adaptive_1.jpg)
