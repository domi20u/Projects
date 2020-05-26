close all
steps = 1000;
t = linspace(0,pi,steps);
dt = 0.01;
x = t;
y = sin(t).^2;
x = sqrt(t);
y = sin(t);
%plot(x,y)
x_des = [x',y'];
goal = x_des(end,end);
x0 = x_des(1,1);
D = 63.25;
K = 1000;

D1 = compute_D1(steps,dt);
D2 = compute_D2(steps,dt);

dx_des = D1*x_des; % np.dot(D1,x_des)
ddx_des = D2*x_des;
writematrix([t',x_des,dx_des,ddx_des],"trajectory_test.txt");
%s_track = exponential_fcn(4,1,steps,1,0);
dmp_scaled = dmp(4,D,1,100,3,1000);
dmp_not_scaled = dmp_scaled;
dmp_scaled_rbf = dmp_scaled;
dmp_not_scaled.rescale = 0;
dmp_scaled_rbf.bf_type = "rbf";
dmp_scaled = dmp_scaled.train("trajectory_test.txt");
dmp_not_scaled = dmp_not_scaled.train("trajectory_test.txt");
dmp_scaled_rbf = dmp_scaled_rbf.train("trajectory_test.txt");

figure('Position',[100 400 1500 400])
subplot(1,3,1)

plot(x,y)
hold on
dmp_scaled.plot_trajectory(0)
dmp_not_scaled.plot_trajectory(0)
dmp_scaled_rbf.plot_trajectory(0)


subplot(1,3,2)
plot(x,y)
hold on
new_goal = [1,0];
dmp_scaled.x_end = new_goal;
dmp_not_scaled.x_end = new_goal;
dmp_scaled_rbf.x_end = new_goal;
dmp_scaled.plot_trajectory(0)
dmp_not_scaled.plot_trajectory(0)
dmp_scaled_rbf.plot_trajectory(0)
legend("original","scaled mullifier","not scaled","scaled rbf")

subplot(1,3,3)
plot(x,y)
hold on
new_goal = [-1.2,1.3];
dmp_scaled.x_end = new_goal;
dmp_scaled.x_init = [-1,-1];
dmp_not_scaled.x_end = new_goal;
dmp_scaled_rbf.x_end = new_goal;
dmp_scaled.plot_trajectory(0)
dmp_not_scaled.plot_trajectory(0)
dmp_scaled_rbf.plot_trajectory(0)
legend("original","scaled with new x end & x init","not scaled with new x end","scaled with new x end")
%f_target= (ddx_des/K-(goal-x_des)+D/K*dx_des)'+(goal-x0)*s_track;


function D1 = compute_D1(n, dt)
    
    D_up = sparse(2:n,1:n-1,ones(1,n-1),n,n);
    D1 = D_up' - D_up;
    D1(1,1) = -3.;
    D1(1,2) = 4.;
    D1(1, 3) = -1.;
    D1(end, end-2) = 1.;
    D1(end,end) = 3.;
    D1(end,end-1) = -4.;
    D1 = D1 / (2 * dt);
end

function D2 = compute_D2(n, dt)
    D_up = sparse(2:n,1:n-1,ones(1,n-1),n,n);
    D0 = sparse(1:n,1:n,2*ones(1,n),n,n);
    D2 = -D0 + D_up' + D_up;
    D2(1,1) = 2.;
    D2(1,2) = -5.;
    D2(1, 3) = 4.;
    D2(1, 4) = -1.;
    D2(end, end-2) = 4.;
    D2(end, end-3) = -1.;
    D2(end,end) = 2.;
    D2(end,end-1) = -5.;
    D2 = D2 / (dt^2);
   
end