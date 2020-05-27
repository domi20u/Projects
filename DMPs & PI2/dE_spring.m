%% Differential equation for a spring-damper-system

function [yd_, zd_] = dE_spring(xs_spring,tau,spring_const,damping,x_end,mass)
y_ = xs_spring(1,1:2);
z_ = xs_spring(1,3:4);
yd_ = z_/tau;
zd_ = (-spring_const*(y_-x_end) - damping*z_)/(mass*tau);
end

