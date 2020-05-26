function traj_t = traj_3D(y_rot,z_rot,filename,fileout)

traj = readmatrix(filename);
ts = traj(:,1);
x = [zeros(length(traj),1),traj(:,2),traj(:,3)];
dx = [zeros(length(traj),1),traj(:,4),traj(:,5)];
ddx = [zeros(length(traj),1),traj(:,6),traj(:,7)];

R2 = [cos(y_rot),0,sin(y_rot);0,1,0;-sin(y_rot),0,cos(y_rot)];
R3 = [cos(z_rot),-sin(z_rot),0;sin(z_rot),cos(z_rot),0;0,0,1];

x_r = x*R2*R3;
dx_r = dx*R2*R3;
ddx_r = ddx*R2*R3;

traj_t = [ts,x_r,dx_r,ddx_r];
if fileout ~= ""
    writematrix(traj_t,fileout)
end
end

