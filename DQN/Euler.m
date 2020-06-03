function [theta_next, d_theta_next, x_next, d_x_next] = Euler(dd_theta, d_theta, theta, dd_x, d_x, x, dt)

d_theta_next = dd_theta*dt + d_theta;
d_x_next = dd_x*dt + d_x;

if d_theta_next > 10
    d_theta_next = 10;
elseif d_theta_next < -10
    d_theta_next = -10;
end

if d_x_next > 10
    d_x_next = 10;
elseif d_x_next < -10
    d_x_next = -10;
end

theta_next = theta + d_theta*dt + 0.5*dd_theta*dt^2;
x_next = x + d_x*dt + 0.5*dd_x*dt^2;

if theta_next > pi
    theta_next = -2*pi + theta_next;
elseif theta_next <= -pi
    theta_next = 2*pi + theta_next;
end
%if x_next > 6
%    x_next = 6;
%elseif x_next < -6
%    x_next = -6;
%end

end

