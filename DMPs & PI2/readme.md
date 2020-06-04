Reproduction of the [dmpbbo demo-robot](https://github.com/roothyb/dmpbbo/tree/master/demo_robot) by Freek Stulp in 2D and 3D.

**Tunable DMPs and optimization parameters for 2D and 3D trajectories. More information summarized in the [PDF](https://github.com/domi20u/Projects/blob/master/DMPs%20%26%20PI2/Praktikum_Report.pdf).**

Running [*test.m*](https://github.com/domi20u/Projects/blob/master/DMPs%20%26%20PI2/test.m):\
Comparison of DMPs with gaussian basis functions and mollifier-like basis functions (introduced by Michele Ginesi in [DMP++](https://github.com/mginesi/dmp_pp)) in 2D and their respective costs wrt. the distance from the goal and the acceleration.
![2D_dmps](https://github.com/domi20u/Projects/blob/master/DMPs%20%26%20PI2/images/dmp_mollifier_rbf.png)

Running [*test3Dnew.m*](https://github.com/domi20u/Projects/blob/master/DMPs%20%26%20PI2/test3Dnew.m):\
DMP with selectable basis functions in 3D and additional rotation parameters to transform the demonstrated 2D trajectory into 3D.
![3D_dmps](https://github.com/domi20u/Projects/blob/master/DMPs%20%26%20PI2/images/dmp3D_bad_better.png)

When tuning the parameters, first check whether the reproduction of the demonstrated trajectory succeeds.
![forcing_terms](https://github.com/domi20u/Projects/blob/master/DMPs%20%26%20PI2/images/forcing_terms_mollifier_2D.png)
![dynamics](https://github.com/domi20u/Projects/blob/master/DMPs%20%26%20PI2/images/demo_repro_dynamics_3D.png)
