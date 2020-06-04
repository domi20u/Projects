function [ tau ] = Tau( u )
%TAU Summary of this function goes here
%   Detailed explanation goes here
% Times are defined in TrajGen.m file
global t0 t1 t2 t3 t4 t5 t6

global mode mode_map
%mode = 32; %modes: 11    12      21     22      23
%                  PD    PD+G

persistent integral dt counter error error_3 t_end Q_traj Qp_traj X_traj Xp_traj X1d X2d
persistent V_traj Vp_traj delta_theta sing mani t_init w_end Q_sing t_fin theta_adapt gamma Q1d Q2d
persistent X_ mani_thresh Q_2
pl = 0;
t=u(1);

if t==0
    integral = [0;0;0];
    dt = str2double(get_param('DSimulator_robot3GDL','FixedStep'));
    counter = 0;
    t_end = str2double(get_param('DSimulator_robot3GDL','StopTime'));
    %t_end = 10;
    t_init = 0;
    error=zeros(round(t_end/dt),1);
    error_3 = zeros(round(t_end/dt),3);
    X_traj = zeros(round(t_end/dt),3);
    Xp_traj = zeros(round(t_end/dt),3);
    Q_traj = zeros(round(t_end/dt),3);
    Qp_traj = zeros(round(t_end/dt),3);
    V_traj = zeros(round(t_end/dt),1);
    Vp_traj = zeros(round(t_end/dt),1);
    delta_theta=zeros(round(t_end/dt),1);
    mani = zeros(round(t_end/dt),1);
    mani_thresh = 0.05;
    sing = 0;
    theta_adapt = zeros(35,1);
    gamma = get_gamma();
    X_ = [0;0;0];
end
DeltaQ = [0;0;0];
DeltaQp=[0;0;0];

Kd=diag([u(11);u(12);u(13)]);
Kp=diag([u(14);u(15);u(16)]);

q1=u(17);
q2=u(18);
q3=u(19);

q1p=u(20);
q2p=u(21);
q3p=u(22);

Q=[q1;q2;q3];
Qp=[q1p;q2p;q3p];

if mode ~= 401
    q1d=deg2rad(u(2));
    q2d=deg2rad(u(3));
    q3d=deg2rad(u(4));

    q1dp=deg2rad(u(5));
    q2dp=deg2rad(u(6));
    q3dp=deg2rad(u(7));
    q1dpp=deg2rad(u(8));
    q2dpp=deg2rad(u(9));
    q3dpp=deg2rad(u(10));
    
    Qd=[q1d;q2d;q3d];
    Qdp=[q1dp;q2dp;q3dp];
    Qdpp=[q1dpp;q2dpp;q3dpp];
    DeltaQ=Q-Qd;
    DeltaQp=Qp-Qdp;
    
end

%Joint Errors


%Robot Parameters

m1=u(23);
m2=u(24);
m3=u(25);
g=u(26);

L1=u(27);
L2=u(28);
L4=u(29);
L6=u(30);
L7=u(31);
L9=u(32);
L3=u(33);
L5=u(34);
L8=u(35);
L10=u(36);

%four input elements missing? 37,38,39, 41,42,43




Kis=diag([u(37);u(38);u(39)]);
Ki=diag([0;0;0]);




gx=u(40);
gy=u(41);
gz=u(42);

I111=u(43);
I112=u(44);
I113=u(45);
I122=u(46);
I123=u(47);
I133=u(48);

I211=u(49);
I212=u(50);
I213=u(51);
I222=u(52);
I223=u(53);
I233=u(54);

I311=u(55);
I312=u(56);
I313=u(57);
I322=u(58);
I323=u(59);
I333=u(60);
I = [I111,I112,I113,I122,I123,I133,I211,I212,I213,I222,I223,I233,I311,I312,I313,I322,I323,I333];

[Hts,J,MCG]=Hts_J_MCG(q1p,q2p,q3p,q1,q2,q3,m1,m2,m3,L1,L2,L3,L4,L5,L6,L7,L8,L9,L10,I,g,gx,gy,gz);
Jef = J{1};
Jefp = J{2};
M = MCG {1};
%Define controllers PD, PD+G
T0_W=[-1 0 0 0;0 -1 0 0;0 0 1 0;0 0 0 1];
T1_0=Hts(:,:,1);
T2_1=Hts(:,:,2);
T3_2=Hts(:,:,3);
T2_0=T1_0*T2_1;
T3_0=T2_0*T3_2;
T3_W=T0_W*T3_0;
X = T3_W(1:3,4);

Xp = Jef(1:3,:)*Qp;
X_ = X_ + Xp*dt;
X = X_;
theta_diff=0;
w=0;

counter = counter + 1;

if mode==11
    %model-free PD
    error_function(Q,Qd)
    DeltaQ = Q-Qd;
    tauc = -Kp*DeltaQ - Kd*Qp;
    V = 1/2*Qp'*M*Qp;
    Vp = Qp'*tauc;
    data_function(X,Xp,Q,Qp,V,Vp,theta_diff,w);
    
elseif mode == 12
    %model-free PD+G
    [~,~,MCG]=Hts_J_MCG(q1p,q2p,q3p,q1,q2,q3,m1,m2,m3,L1,L2,L3,L4,L5,L6,L7,L8,L9,L10,I,g,gx,gy,gz);
    G = MCG{3};
    error_function(Q,Qd)
    
    tauc = -Kp*DeltaQ - Kd*Qp + G;
    V = 1/2*Qp'*M*Qp;
    Vp = Qp'*tauc -Qp'*G;
    data_function(X,Xp,Q,Qp,V,Vp,theta_diff,w);
elseif mode == 21
    %break
    Qrp = [0;0;0];
    Qrpp = [0;0;0];
    [Yr,Theta] = Yr_Theta(Q,Qp,Qrp,Qrpp,m1,m2,m3,L1,L2,L3,L4,L5,L6,L7,L8,L9,L10,I,g,gx,gy,gz);
    error_function(Q,[0;0;0])
    
    Sq = Qp - Qrp;
    tauc = -Kd*Sq +Yr*Theta;
    V = 0.5*Sq'*M*Sq;
    Vp = Sq'*tauc - Sq'*Yr*Theta;
    data_function(X,Xp,Q,Qp,V,Vp,theta_diff,w);
    
elseif mode == 22
    %regressor PD-like controller
    Qrp = Qdp - Kp*DeltaQ;
    Qrpp = Qdpp-Kp*DeltaQp;
    [Yr,Theta] = Yr_Theta(Q,Qp,Qrp,Qrpp,m1,m2,m3,L1,L2,L3,L4,L5,L6,L7,L8,L9,L10,I,g,gx,gy,gz);
    error_function(Q,Qd)
    
    Sq = Qp - Qrp;
    tauc = -Kd*Sq +Yr*Theta;
    V = 0.5*Sq'*M*Sq;
    Vp = Sq'*tauc - Sq'*Yr*Theta;
    data_function(X,Xp,Q,Qp,V,Vp,theta_diff,w);
elseif mode == 23
    %regressor PID-like controller
    integral = integral + DeltaQ*dt;
    Qrp = Qdp - Kp*DeltaQ - Ki*integral;
    Qrpp = Qdpp-Kp*DeltaQp-Ki*DeltaQ;
    [Yr,Theta] = Yr_Theta(Q,Qp,Qrp,Qrpp,m1,m2,m3,L1,L2,L3,L4,L5,L6,L7,L8,L9,L10,I,g,gx,gy,gz);
    error_function(Q,Qd)
    
    Sq = Qp - Qrp;
    tauc = -Kd*Sq +Yr*Theta;
    V = 0.5*Sq'*M*Sq;
    Vp = Sq'*tauc - Sq'*Yr*Theta;
    data_function(X,Xp,Q,Qp,V,Vp,theta_diff,w);
elseif mode == 41 || mode == 42 || mode == 51 || mode == 52 || mode == 8 || mode == 31 || mode == 32
    Jv = Jef(1:3,:);
    w = sqrt(det(Jv*Jv'));
    %disp(Qd)
    %if mode == 41
    %Kd = diag([100,75,75]);
    %Kp = diag([50,612.718,612.718]);
    
        if t <= t1
            
            Qd_0 = [0;0;pi/2];
            [Qd,Qdp,Qdpp] = gen_trajectory([0;0;0],Qd_0,0,t1,t);
            error_function(Q,Qd);
            DeltaQ = Q-Qd;
            DeltaQp = Qp-Qdp;
            if mode == 41 || mode == 51 || mode == 8 || mode == 31 
                Qrp = Qdp - Kp*DeltaQ;
                Qrpp = Qdpp -Kp*DeltaQp;
            else
                integral = integral + DeltaQ*dt;
                Qrp = Qdp - Kp*DeltaQ - Ki*integral;
                Qrpp = Qdpp-Kp*DeltaQp-Ki*DeltaQ;
            end
        
        
        else
            if w > mani_thresh && sing==0 || mode == 31 || mode == 32
                
                if t <= t2
                    if t<(t1+dt+dt/2)&&t>(t1+dt-dt/2)
                        %disp("t+dt")
                        X1d = X;
                        X2d = X1d;
                        X2d(1)=X2d(1)+0.9;
                        X2d(2)=X2d(2)-0.3;
                        X2d(3) = X2d(3)+0.4;
                        %disp("finished t+dt")
                        Q1d = Q;
                        
                        %derivation: see line ~277
                        Q2d = [1.4723;-0.034;1.0057];
                        
                    end
                    [Xd,Xdp,Xdpp] = gen_trajectory(X1d,X2d,t1+dt,t2,t);
                    [Qd,Qdp,Qdpp] = gen_trajectory(Q1d,Q2d,t1+dt,t2,t);
                    
                    %X = T3_W(1:3,4);
                    %Xp = Jef(1:3,:)*Qp;
                    error_function(X,Xd)
                    DeltaX = X-Xd;
                    DeltaXp = Xp-Xdp;
                    DeltaQ = Q-Qd;
                    DeltaQp = Qp-Qdp;
                    if mode == 41 || mode == 51
                        Qrp = Jef(1:3,:)\(Xdp - Kp/Kd*DeltaX);
                        Qrpp = Jef(1:3,:)\(Xdpp-Kp/Kd*DeltaXp-Jefp(1:3,:)*Qrp);
                    elseif mode == 8 || mode == 32
                        integral = integral + DeltaQ*dt
                        Qrp = Qdp - Kp*DeltaQ - Ki*integral;
                        Qrpp = Qdpp-Kp*DeltaQp-Ki*DeltaQ;
                    elseif mode == 31
                        Qrp = Qdp -Kp*DeltaQ;
                        Qrpp = Qdpp-Kp*DeltaQp;
                    else
                        integral = integral + DeltaX*dt;
                        Qrp = Jef(1:3,:)\(Xdp - Kp/Kd*DeltaX - Ki/Kd*integral);
                        Qrpp = Jef(1:3,:)\(Xdpp-Kp/Kd*DeltaXp-Ki/Kd*DeltaX-Jefp(1:3,:)*Qrp);
                    end
         % To find the same X2d position in joint space for mode == 8
                    if t == t2
                        Q_2 = Q;
                        %integral = [0;0;0];
                    end
                elseif mode == 31 || mode == 32
                    r=0.2;
                    a=0.05;
                    cx = Q_2(1)-r;
                    cy = Q_2(2);
                    cz = Q_2(3)-a;
                    if t<=t0
                        Qd = Q;
                        Qdp = [0;0;0];
                        Qdpp = [0;0;0];
                    elseif t<=t3
                        omega=0.3;
                        
                        Qd = [r*cos(omega*(t-t2))+cx;r*sin(omega*(t-t2))+cy;a*cos(10*omega*(t-t2))+cz];
                        Qdp = [-omega*r*sin(omega*(t-t2));omega*r*cos(omega*(t-t2));-10*omega*a*sin(10*omega*(t-t2))];
                        Qdpp = [-omega^2*r*cos(omega*(t-t2));-omega^2*r*sin(omega*(t-t2));-100*omega^2*a*cos(10*omega*(t-t2))];
                        if t == t3
                            X2d = X;
                        end
                    else
                        omega=0.6;
                        
                        Qd = [r*cos(omega*(t-t3))+cx;r*sin(omega*(t-t3))+cy;a*cos(10*omega*(t-t3))+cz];
                        Qdp = [-omega*r*sin(omega*(t-t3));omega*r*cos(omega*(t-t3));-10*omega*a*sin(10*omega*(t-t3))];
                        Qdpp = [-omega^2*r*cos(omega*(t-t3));-omega^2*r*sin(omega*(t-t3));-100*omega^2*a*cos(10*omega*(t-t3))];
                    end
                    DeltaQ = Q-Qd;
                    DeltaQp = Qp-Qdp;
                    error_function(Q,Qd)
                    if mode == 32
                        integral = integral + DeltaQ*dt
                        Qrp = Qdp - Kp*DeltaQ - Ki*integral;
                        Qrpp = Qdpp-Kp*DeltaQp-Ki*DeltaQ;
                    else
                        Qrp = Qdp -Kp*DeltaQ;
                        Qrpp = Qdpp-Kp*DeltaQp;
                    end
                else
                    
            %operational space - circle
                    a = 0.01;
                    if mode == 51 || mode == 52
                        r=0.3;
                    else
                        r=0.1;
                    end
                    if t <= t0
                        Xd = X2d;
                        Xdp = [0;0;0];
                        Xdpp = [0;0;0];
                    elseif t<=t3
                        omega=0.3;
                    
                        cx = X2d(1)-r;
                        cy = X2d(2);
                        cz = X2d(3)-a;
                        Xd = [r*cos(omega*(t-t2))+cx;r*sin(omega*(t-t2))+cy;a*cos(10*omega*(t-t2))+cz];
                        Xdp = [-omega*r*sin(omega*(t-t2));omega*r*cos(omega*(t-t2));-10*omega*a*sin(10*omega*(t-t2))];
                        Xdpp = [-omega^2*r*cos(omega*(t-t2));-omega^2*r*sin(omega*(t-t2));-100*omega^2*a*cos(10*omega*(t-t2))];
                        if t == t3
                            X2d = X;
                        end
                    else
                        omega=0.6;
                        cx = X2d(1)-r;
                        cy = X2d(2);
                        cz = X2d(3)-a;
                        Xd = [r*cos(omega*(t-t3))+cx;r*sin(omega*(t-t3))+cy;a*cos(10*omega*(t-t3))+cz];
                        Xdp = [-omega*r*sin(omega*(t-t3));omega*r*cos(omega*(t-t3));-10*omega*a*sin(10*omega*(t-t3))];
                        Xdpp = [-omega^2*r*cos(omega*(t-t3));-omega^2*r*sin(omega*(t-t3));-100*omega^2*a*cos(10*omega*(t-t3))];
                    end
                    X = Jef(1:3,:)*Q;
                    X = X_;
                    Xp = Jef(1:3,:)*Qp;
                    error_function(X,Xd)
                    DeltaX = X-Xd;
                    DeltaXp = Xp-Xdp;
                    if mode == 41 || mode == 51
                        Qrp = Jef(1:3,:)\(Xdp - Kp/Kd*DeltaX);
                        Qrpp = Jef(1:3,:)\(Xdpp-Kp/Kd*DeltaXp-Jefp(1:3,:)*Qrp);
                    else
                        integral = integral + DeltaQ*dt;
                        Qrp = Jef(1:3,:)\(Xdp - Kp/Kd*DeltaX - Ki/Kd*integral);
                        Qrpp = Jef(1:3,:)\(Xdpp-Kp/Kd*DeltaXp-Ki/Kd*DeltaX-Jefp(1:3,:)*Qrp);
                    end
                end
            else
                if sing==0
                    w_end = w;
                    disp(w_end)
                    disp(t)
                    sing=1;
                    Qrp = [0;0;0];
                    Qrpp = [0;0;0];
                    Q_sing = Q;
                    t_init = t;
                    if (t_end-t_init) > 5
                        t_fin = t_init+5;
                    else
                        t_fin = t_end;
                    end
                    Qd = Q;
                    error_function(Q,Qd)
                else
                    if t <= t_fin
                        [Qd,Qdp,Qdpp] = gen_trajectory(Q_sing,[0;0;0],t_init+dt,t_fin,t);
                        DeltaQ = Q-Qd;
                        DeltaQp = Qp-Qdp;
                        error_function(Q,Qd)
                        if mode == 41 || mode == 51
                            Qrp = Qdp - Kp*DeltaQ;
                            Qrpp = Qdpp -Kp*DeltaQp;
                        else
                            integral = integral + DeltaQ*dt;
                            Qrp = Qdp - Kp*DeltaQ - Ki*integral;
                            Qrpp = Qdpp-Kp*DeltaQp-Ki*DeltaQ;
                        end
                    else
                        Qrp = [0;0;0];
                        Qrpp = [0;0;0];
                        Qd = Q;
                        error_function(Q,Qd)
                    end
                end
                
            end
        end       
    [Yr,Theta] = Yr_Theta(Q,Qp,Qrp,Qrpp,m1,m2,m3,L1,L2,L3,L4,L5,L6,L7,L8,L9,L10,I,g,gx,gy,gz);
    Sq = Qp - Qrp;
    tauc = -Kd*Sq +Yr*Theta;
    V = 0.5*Sq'*M*Sq;
    Vp = Sq'*tauc -Sq'*Yr*Theta;
    if mode == 8
        %gamma = diag([0.1,5,0.05]);
        theta_d = -gamma\Yr'*Sq;
        theta_adapt = theta_adapt + theta_d*dt;
        tauc = -Kd*Sq + Yr*theta_adapt;
        Vp = Sq'*tauc -Sq'*Yr*theta_adapt;
        theta_diff = Theta-theta_adapt;
    end
    
    
    data_function(X,Xp,Q,Qp,V,Vp,theta_diff,w)
    
end

if t==t_end
    if pl == 1
        
        figure
        x = 0:dt:t_end;
        plot(x,error)
        title("Accumulated Error")
        xlabel('t')
        xline(t1,':');
        xline(t2,':');
        xline(t3,':');
        
        figure
        plot(x,error_3)
        title("Error")
        xlabel('t')
        xline(t1,':');
        xline(t2,':');
        xline(t3,':');
        
        figure
        plot(x,X_traj)
        title("X")
        xlabel('t')
        xline(t1,':');
        xline(t2,':');
        xline(t3,':');
        
        figure
        plot(x,Xp_traj)
        title("Xp")
        xlabel('t')
        xline(t1,':');
        xline(t2,':');
        xline(t3,':');
        
        figure
        plot(x,Q_traj)
        title("Q")
        xlabel('t')
        legend('q1','q2','q3')
        xline(t1,':');
        xline(t2,':');
        xline(t3,':');
        
        figure
        plot(x,Qp_traj)
        title("Qp")
        xlabel('t')
        xline(t1,':');
        xline(t2,':');
        xline(t3,':');
        
        figure
        plot(x,V_traj)
        title("V")
        xlabel('t')
        xline(t1,':');
        xline(t2,':');
        xline(t3,':');
        
        figure
        plot(x,Vp_traj)
        title("Vp")
        xlabel('t')
        xline(t1,':');
        xline(t2,':');
        xline(t3,':');
        
        figure
        plot(x,delta_theta)
        title("Delta Theta")
        xlabel('t')
        xline(t1,':');
        xline(t2,':');
        xline(t3,':');
        
        figure
        plot(x,mani)
        title("Manipulability")
        xlabel('t')
        xline(t1,':');
        xline(t2,':');
        xline(t3,':');
    else
        if mode == 11 || mode == 12
           t0 = 0;
        end
        figure
        set(gcf,'position',[300,0,800,830])
        x = 0:dt:t_end;
        subplot(5,2,1)
        plot(x,error)
        title("Accumulated Error")
        xline(t1,':');
        xline(t2,':');
        xline(t3,':');
        xline(t_init,'--r');
        xline(t0,':');
        
        subplot(5,2,2)
        plot(x,error_3)
        title("Error")
        xline(t1,':');
        xline(t2,':');
        xline(t3,':');
        xline(t_init,'--r');
        xline(t0,':');
        
        subplot(5,2,3)
        plot(x,X_traj)
        title("X")
        xline(t1,':');
        xline(t2,':');
        xline(t3,':');
        xline(t_init,'--r');
        xline(t0,':');
        
        subplot(5,2,4)
        plot(x,Xp_traj)
        title("Xp")
        xline(t1,':');
        xline(t2,':');
        xline(t3,':');
        xline(t_init,'--r');
        xline(t0,':');
        
        subplot(5,2,5)
        plot(x,Q_traj)
        title("Q")
        xline(t1,':');
        xline(t2,':');
        xline(t3,':');
        xline(t_init,'--r');
        xline(t0,':');
        
        subplot(5,2,6)
        plot(x,Qp_traj)
        title("Qp")
        xline(t1,':');
        xline(t2,':');
        xline(t3,':');
        xline(t_init,'--r');
        xline(t0,':');
        
        subplot(5,2,7)
        plot(x,V_traj)
        title("V")
        xline(t1,':');
        xline(t2,':');
        xline(t3,':');
        xline(t_init,'--r');
        xline(t0,':');
        
        subplot(5,2,8)
        plot(x,Vp_traj)
        title("Vp")
        xline(t1,':');
        xline(t2,':');
        xline(t3,':');
        xline(t_init,'--r');
        xline(t0,':');
        
        subplot(5,2,9)
        plot(x,delta_theta)
        title("Delta Theta")
        xline(t1,':');
        xline(t2,':');
        xline(t3,':');
        xline(t_init,'--r');
        xline(t0,':');
        
        subplot(5,2,10)
        plot(x,mani)
        title("Manipulability")
        xline(t1,':');
        xline(t2,':');
        xline(t3,':');
        xline(t_init,'--r');
        xline(t0,':');
        yline(mani_thresh,':');
        sgtitle(mode_map(mode))
        
    end
end
    
tau=[tauc;DeltaQ;DeltaQp];

function error_function(in1,in2)
    error(counter) = sqrt((in1-in2)'*(in1-in2));
    error_3(counter,:) = in1-in2;
end

function data_function(X, Xp, Q, Qp, V, Vp, theta_diff, w)
    
    X_traj(counter,:) = X;
    Xp_traj(counter,:)=Xp;
    Q_traj(counter,:)=Q;
    Qp_traj(counter,:)=Qp;
    V_traj(counter,:)=V;
    Vp_traj(counter,:)=Vp;
    delta_theta(counter) = sqrt(theta_diff'*theta_diff);
    mani(counter) = w;
end


function test_traj(p_init,p_final,t_init,t_final)

tv=t_init:.1:t_final;

traj_p = zeros(length(tv),3);
traj_pd=zeros(length(tv),3);
traj_pdd=zeros(length(tv),3);

for i=1:length(tv)
    [traj_p(i,:),traj_pd(i,:),traj_pdd(i,:)]=gen_trajectory(p_init,p_final,t_init,t_final,tv(i));
end

figure
hold on
subplot(3,1,1)
plot(tv,traj_p)
legend('x1','x2','x3')
subplot(3,1,2)
plot(tv,traj_pd)
subplot(3,1,3)
plot(tv,traj_pdd)
hold off
end

end

