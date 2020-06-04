function [qd] = TrajGen( u )
%TRAJGEN Summary of this function goes here
%   Detailed explanation goes here

global t0 t1 t2 t3 t4 t5 t6
global mode
global mode_map
persistent t_step t_end 

mode = 8;
mode_map = containers.Map({11,12,21,22,23,31,32,41,42,51,52,8},{"Free PD","Free PD+G",...
    "Model -> Break","Model -> PD-like - Joint Space","Model -> PID-like - Joint Space",...
    "Model -> PD-like - Joint Space 2", "Model -> PID-like - Joint Space 2",...
    "Model -> PD-like - Operational Space","Model -> PID-like - Operational Space",...
    "Model -> PD-like - Operational Space: Safety Mechanism",...
    "Model -> PID-like - Operational Space: Safety Mechanism", "Adaptive Controller"});


t_end = str2double(get_param('DSimulator_robot3GDL','StopTime'));


t=u(4);
t_step = round(t_end/11);
t1 = t_step*2;
t2 = t1 + t_step*2;
t0 = t2 + t_step;
t3 = t0 + t_step*3;
t4 = t_end;

    


% Interval between PD, PD+G, and PID+G
% Generate Different trajectories at different times

q1d_0 = u(1);
q2d_0 = u(2);
q3d_0 = u(3);


Qd_0 = [q1d_0;q2d_0;q3d_0];

%% Compare PD & PD+G
    if t <= t1
        Qd=Qd_0;
        Qdp=[0;0;0];
        Qdpp=[0;0;0];
        
    elseif t <= t2
        Qd = Qd_0;
        Qdp=[0;0;0];
        Qdpp=[0;0;0];
    else
        
        if t<=t3
            omega = 0.3;
        else
            omega = 0.6;
        end
        A = 10;
        Qd = A*[cos(omega*t);sin(omega*t);cos(omega*t)]+Qd_0;
        Qdp = A*[-omega*sin(omega*t);omega*cos(omega*t);-omega*sin(omega*t)];
        Qdpp = A*[-omega^2*cos(omega*t);-omega^2*sin(omega*t);-omega^2*cos(omega*t)];
    end                
 
%qd=[q1d;q2d;q3d;q1dp;q2dp;q3dp;q1dpp;q2dpp;q3dpp];
qd = [Qd;Qdp;Qdpp];


end

