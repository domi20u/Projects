close all

saving = 0;
dir = "results_3D/opt01";

alpha = 4;      %4
damping_coefficient = 20; %20
mass = 1; %1
n_rbf = 10; %10
rbf_sigma = 1.5;%1.5;
sigma = 3.5;%3.5;
n_time_steps = 287; %200

rbf_dmp = dmp3D(alpha,damping_coefficient,mass,n_rbf,rbf_sigma,n_time_steps);
rbf_dmp.bf_type = "rbf";
%rbf_dmp.no_exp = 1;
mullifier_dmp = dmp3D(alpha,damping_coefficient,mass,n_rbf,sigma,n_time_steps);
%mullifier_dmp.no_exp = 1;
rbf_dmp = rbf_dmp.train("traj3D.txt");

mullifier_dmp = mullifier_dmp.train("traj3D.txt");
mullifier_dmp.plot_rollout(0)

writematrix([rbf_dmp.mean_x,rbf_dmp.mean_y,rbf_dmp.mean_z],"results_3D/orig_rbf_means.txt")
writematrix([mullifier_dmp.mean_x,mullifier_dmp.mean_y,mullifier_dmp.mean_z],"results_3D/orig_mullifier_means.txt")
%disp(size(test_dmp.mean_x))
%[y,~,y_ball] = test_dmp.dynamics(test_dmp.mean_x,test_dmp.mean_y,0,"");
%mean_expl=test_dmp.explore(5);
%disp(size(means_x))

max_runs = 20;
n_samples = 5;
h = 10;
covar_decay_factor = 0.95;
covar_decay_factor_rbf = 0.9;



n_opt = 1;

costs_rbf = zeros(n_opt,max_runs);
costs_mullifier = zeros(n_opt,max_runs);

init_pertub = [0,0,0];
goal_pertub = [0,0,0];
rbf_dmp.x_end = rbf_dmp.x_end + goal_pertub;
mullifier_dmp.x_end = mullifier_dmp.x_end + init_pertub;
rbf_dmp.x_init = rbf_dmp.x_init + goal_pertub;
mullifier_dmp.x_init = mullifier_dmp.x_init + init_pertub;


figure('Position',[100 400 700 400])
hold on
for i = 1:n_opt
    fprintf("Start RBF-Optimization #%d/%d\n",i,n_opt)
    directory_rbf = dir+"/rbf_"+i;
    if saving
        mkdir(directory_rbf);
    end
    [~,costs_rbf(i,:)] = rbf_dmp.optimize(max_runs,n_samples,h,covar_decay_factor_rbf,saving,directory_rbf);
end
figure('Position',[100+700 400 700 400])
hold on
for i = 1:n_opt
    fprintf("Start Mullifier-Optimization #%d/%d\n",i,n_opt)
    directory_mullifier = dir+"/mullifier_"+i;
    if saving
        mkdir(directory_mullifier);
    end
    [~,costs_mullifier(i,:)] = mullifier_dmp.optimize(max_runs,n_samples,h,covar_decay_factor,saving,directory_mullifier);
end

avg_costs_rbf = mean(costs_rbf);
avg_costs_mullifier = mean(costs_mullifier);


figure('Position',[100+700+700 400 700 400])
hold on
plot(avg_costs_rbf)
plot(avg_costs_mullifier)
legend("RBF Costs","Mullifier Costs")
%test_dmp.plot_rollout(mean_expl)