%% Example of using the dmp class and the rbfn functions
%% Reproducing the results of the "throwing task" from dmpbbo 
%by Freek Stulp: https://github.com/roothyb/dmpbbo/tree/master/demo_robot

close all

%Rotation of the original 2D trajectory into 3D
rotY = pi/20;                       %default: pi/20
rotZ = pi/10;                        %default: pi/10

damping_coefficient = 20;           %default: 20
n_rbf =20;                         %default: 10
sigma = 1.5;                        %default: 3.5
n_time_steps = 250;                 %default: 200
max_opt_runs = 140;
n_opt_samples = 5;
covar_decay_factor = 0.995;          %default: 0.95;
init_pertub = [0,0,0];
goal_pertub = [0,0,0];
ball_goal_pertub = [-0.2,0.2];
basis_function_type = "mollifier";  %mollifier, else rbf


%Option of saving the resulting trajectories during optimization
saving = 0;
dir = "results_3D/opt01";
%Set constant values
alpha = 4;                          %default: 4
mass = 1;                           %default: 1
rbf_sigma = sigma/2;

%Create a 3D-trajectory by tranforming the original 2D trajectory
traj_3D(rotY,rotZ,"trajectory.txt","traj3D.txt");

%Initialize two dmps with radial and mollifier basis functions
dmp_3D = dmp3D(alpha,damping_coefficient,mass,n_rbf,rbf_sigma,n_time_steps);
dmp_3D.bf_type = basis_function_type;
%Train dmp with demonstrated trajectory from "trajectory.txt"
dmp_3D = dmp_3D.train("traj3D.txt");

%Set optimization parameters
h = 10;
n_opt = 1;
costs = zeros(n_opt,max_opt_runs,3);
dmp_3D.ball_goal = dmp_3D.ball_init+ball_goal_pertub;
   
%optional: adjust intial and goal positions
dmp_3D.x_end = dmp_3D.x_end + goal_pertub;
dmp_3D.x_init = dmp_3D.x_init + init_pertub;

%Start optimization for dmp with RBFNs

for i = 1:n_opt
    fprintf("Start Optimization\n")% #%d/%d\n",i,n_opt)
    directory = dir+"/dmp_"+i;
    if saving
        mkdir(directory);
    end
    [~,costs(i,:,:)] = dmp_3D.optimize(max_opt_runs,n_opt_samples,h,covar_decay_factor,saving,directory);
end






