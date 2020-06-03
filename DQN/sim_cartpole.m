%% Simulation during training
function sim_cartpole(x,x_n,theta,theta_n,a)
f=figure(2);
clf(f)
hold on;
grid on;
l=0.6;
plot([x; x+sin(-theta)*l], [0.2; 0.2+cos(-theta)*l], 'k', 'Linewidth', 1);
plot([-0.5+x, 0.5+x], [0.2, 0.2], 'k', 'Linewidth', 1);
plot([-0.5+x, 0.5+x], [0, 0], 'k', 'Linewidth', 1);
plot([-0.5+x, -0.5+x], [0.2, 0], 'k', 'Linewidth', 1);
plot([0.5+x, 0.5+x], [0, 0.2], 'k', 'Linewidth', 1);
plot([0;a/2],[2;2],'b','Linewidth',3);
axis equal;
a = 6;
axis([-a a -a a]);
set(gcf, 'Position',  [2000, 200, 700, 800])
pause on;


end

