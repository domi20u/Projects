%% Example of using the dmp class and the rbfn functions
%% Reproducing the results of the "throwing task" from dmpbbo 
%by Freek Stulp: https://github.com/roothyb/dmpbbo/tree/master/demo_robot

close all

%Option of saving the resulting trajectories during optimization
saving = 0;
dir = "results/opt01";

%Set constant values
alpha = 4;                          %default: 4
damping_coefficient = 20;           %default: 20
mass = 1;                           %default: 1
n_rbf = 50;                         %default: 10
rbf_sigma = 1.1;                    %default: 1.5
sigma = 3.0;                        %default: 3.5
n_time_steps = 200;                 %default: 200

%Initialize two dmps with radial and mollifier basis functions
rbf_dmp = dmp(alpha,damping_coefficient,mass,n_rbf,rbf_sigma,n_time_steps);
rbf_dmp.bf_type = "rbf";
mollifier_dmp = dmp(alpha,damping_coefficient,mass,n_rbf,sigma,n_time_steps);

%Train both dmps with demonstrated trajectory from "trajectory.txt"
rbf_dmp = rbf_dmp.train("trajectory.txt");
mollifier_dmp = mollifier_dmp.train("trajectory.txt");

%Set optimization parameters
max_runs = 30;
n_samples = 5;
h = 10;
covar_decay_factor = 0.98;
covar_decay_factor_rbf = 0.95;
n_opt = 1;
costs_rbf = zeros(n_opt,max_runs,3);
costs_mollifier = zeros(n_opt,max_runs,3);

%optional: adjust intial and goal positions
init_pertub = [0,0];
goal_pertub = [0,0];
rbf_dmp.x_end = rbf_dmp.x_end + goal_pertub;
mollifier_dmp.x_end = mollifier_dmp.x_end + init_pertub;
rbf_dmp.x_init = rbf_dmp.x_init + goal_pertub;
mollifier_dmp.x_init = mollifier_dmp.x_init + init_pertub;

%Start optimization for dmp with RBFNs
figure('Position',[50 50 1500 900])
subplot(2,2,1)
title("RBF Trajectories")
hold on
for i = 1:n_opt
    fprintf("Start RBF-Optimization\n")% #%d/%d\n",i,n_opt)
    directory_rbf = dir+"/rbf_"+i;
    if saving
        mkdir(directory_rbf);
    end
    [~,costs_rbf(i,:,:)] = rbf_dmp.optimize(max_runs,n_samples,h,covar_decay_factor_rbf,saving,directory_rbf);
end

%Start optimization for dmp with Mollifier-like basis functions
%figure('Position',[100+700 400 700 400])
subplot(2,2,2)
title("Mollifier Trajectories")
hold on
for i = 1:n_opt
    fprintf("Start Mollifier-Optimization\n")% #%d/%d\n",i,n_opt)
    directory_mollifier = dir+"/mollifier_"+i;
    if saving
        mkdir(directory_mollifier);
    end
    [~,costs_mollifier(i,:,:)] = mollifier_dmp.optimize(max_runs,n_samples,h,covar_decay_factor,saving,directory_mollifier);
end

%Distinguish one optimization run and several runs (take the mean costs
%from multiple optimizations)
if n_opt > 1
    avg_costs_rbf = mean(costs_rbf);
    avg_costs_mollifier = mean(costs_mollifier);
else
    avg_costs_rbf = costs_rbf;
    avg_costs_mollifier = costs_mollifier;
end

%Plot the costs
%figure('Position',[100+700+700 400 700 400])
subplot(2,2,3)
hold on
%plot(avg_costs_rbf(1,:,1))
plot(avg_costs_rbf(1,:,1))
plot(avg_costs_rbf(1,:,2))
plot(avg_costs_rbf(1,:,3))
legend("Full Costs","Distance Cost","Acceleration Cost")
title("RBF Costs")

subplot(2,2,4)
hold on
%plot(avg_costs_rbf(1,:,1))
plot(avg_costs_mollifier(1,:,1))
plot(avg_costs_mollifier(1,:,2))
plot(avg_costs_mollifier(1,:,3))
legend("Full Costs","Distance Cost","Acceleration Cost")
title("Mollifier Costs")
%legend("RBF Costs","Mollifier Costs")