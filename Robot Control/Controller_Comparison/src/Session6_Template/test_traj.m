function test_traj(p_init,p_final,t_init,t_final)


t=t_init:.1:t_final;

traj_p = zeros(length(t),3);
traj_pd=zeros(length(t),3);
traj_pdd=zeros(length(t),3);

for i=1:length(t)
    [traj_p(i,:),traj_pd(i,:),traj_pdd(i,:)]=gen_trajectory(p_init,p_final,t_init,t_final,t(i));
end

figure(1)
hold on
subplot(3,1,1)
plot(t,traj_p)
legend('x1','x2','x3')
subplot(3,1,2)
plot(t,traj_pd)
subplot(3,1,3)
plot(t,traj_pdd)
hold off
end

