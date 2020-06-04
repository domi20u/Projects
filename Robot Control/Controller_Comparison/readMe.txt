1)
Parameters for PD & PD+G control:
	Q_initial: [pi pi/2 pi/2]
	Q_target: [0 0 0]
	Kd: [3 15 10]
	Kp: [2 15 10]

Result: Much faster convergence of the error to zero with the PD+G controller. The V-function also converges slightly faster. Wrt to Lyapunov theory, both function have a small unstable beginning with an increasing V. However, the one of PD+G is smaller.

[include 2x3 plots]




