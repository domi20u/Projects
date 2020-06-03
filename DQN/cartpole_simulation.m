%% function that simulates the cartpole in "real-time"
% 500 iteration a 0.01s -> 5s simulation

function cartpole_simulation(x_sequence,theta_sequence,action_sequence)
f=figure(2);
clf(f)

l = 0.6;
dt = 0.0095;              % Time step
iters = length(action_sequence);

x = 0;
theta = pi;

hold on;
grid on;
pend=plot([x; x-sin(theta)*l], [0.2; 0.2+cos(theta)*l], 'k', 'Linewidth', 1);
car_top=plot([-0.5+x, 0.5+x], [0.2, 0.2], 'k', 'Linewidth', 1);
car_bottom=plot([-0.5+x, 0.5+x], [0, 0], 'k', 'Linewidth', 1);
car_left=plot([-0.5+x, -0.5+x], [0.2, 0], 'k', 'Linewidth', 1);
car_right=plot([0.5+x, 0.5+x], [0, 0.2], 'k', 'Linewidth', 1);
f_plot=plot([0;0],[2;2],'b','Linewidth',3);
axis equal;
ax = 6;
axis([-6 6 -1 3]);
set(gcf, 'Position',  [200, 50, 700, 700])
pause on;

for i = 1:iters

    pend.XData = [x_sequence(i); x_sequence(i)-sin(theta_sequence(i))*l];
    pend.YData = [0.2; 0.2+cos(theta_sequence(i))*l];
    car_top.XData = [-0.5+x_sequence(i); 0.5+x_sequence(i)];
    car_bottom.XData = [-0.5+x_sequence(i); 0.5+x_sequence(i)];
    car_left.XData = [-0.5+x_sequence(i); -0.5+x_sequence(i)];
    car_right.XData = [0.5+x_sequence(i); 0.5+x_sequence(i)];
    f_plot.XData = [0;action_sequence(i)/2];
    
    pause(dt)
    
end
