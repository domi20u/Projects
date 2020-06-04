function [p_current,pd_current,pdd_current] = gen_trajectory(p_init,p_final,t_init,t_final,t_current)
persistent a 
    a=poly_init(t_init,t_final);
    
%end
%a(1)=a(1)+p_init;
diff_x=p_final(1)-p_init(1);
diff_y=p_final(2)-p_init(2);
diff_z=p_final(3)-p_init(3);
%a(4)=a(4)*diff;
%a(5)=a(5)*diff;
poly_0= a(1)+a(2)*t_current+a(3)*t_current^2+a(4)*t_current^3+a(5)*t_current^4+a(6)*t_current^5;
x_current=poly_0*diff_x+p_init(1);
y_current=poly_0*diff_y+p_init(2);
z_current=poly_0*diff_z+p_init(3);
p_current=[x_current;y_current;z_current];

poly_d0=a(2)+2*a(3)*t_current+3*a(4)*t_current^2+4*a(5)*t_current^3+5*a(6)*t_current^4;
xd_current=poly_d0*diff_x;
yd_current=poly_d0*diff_y;
zd_current=poly_d0*diff_z;
pd_current=[xd_current;yd_current;zd_current];

poly_dd0 = 2*a(3)+6*a(4)*t_current+12*a(5)*t_current^2+20*a(6)*t_current^3;
xdd_current = poly_dd0*diff_x;
ydd_current = poly_dd0*diff_y;
zdd_current = poly_dd0*diff_z;
pdd_current = [xdd_current,ydd_current,zdd_current];




%close all;
%hold on;
%samples=linspace(t_init,t_final,100);
%x=zeros(100,1);
%y=zeros(100,1);
%z=zeros(100,1);
%y_d=zeros(100,1);
%y_dd=zeros(100,1);
%count=0;

%for i=samples
    %count=count+1;
    %poly_0= a(1)+a(2)*i+a(3)*i^2+a(4)*i^3+a(5)*i^4+a(6)*i^5;
    %x(count)=poly_0*diff_x+p_init(1);
    %y(count)=poly_0*diff_y+p_init(2);
    %z(count)=poly_0*diff_z+p_init(3);
    %y_d(count)=a(2)+2*a(3)*i+3*a(4)*i^2+4*a(5)*i^3+5*a(6)*i^4;
    %y_d(count)=y_d(count)*diff;
    %y_dd(count)=2*a(3)+6*a(4)*i+12*a(5)*i^2+20*a(6)*i^3;
    %y_dd(count)=y_dd(count)*diff;
%end
%x=linspace(1,10,100);
%subplot(4,1,1)
%plot(x)
%subplot(4,1,2)
%plot(y)
%subplot(4,1,3)
%plot(z)
%subplot(4,1,4)
%plot3(x,y,z)
