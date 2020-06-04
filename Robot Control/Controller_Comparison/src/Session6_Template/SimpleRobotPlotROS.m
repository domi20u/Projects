function [ Xout ] = SimpleRobotPlotROS( u )
%SIMPLEROBOTPLOT Summary of this function goes here
persistent  jointpub jointmsg counter tftree tfStampedMsg tfStampedMsg1 tfStampedMsg2 tfStampedMsg3
persistent error t_end
global mode
%Joint Position
q1=u(1);
q2=u(2);
q3=u(3);

%Joint Velocity
qp1=u(4);
qp2=u(5);
qp3=u(6);


%Kinematic Parameters
L1=u(7);
L2=u(8);
L4=u(9);
L6=u(10);
L7=u(11);
L9=u(12);
L3=u(13);
L5=u(14);
L8=u(15);
L10=u(16);


%Time
t=u(39);

%Desired joint position
q1d=deg2rad(u(55));
q2d=deg2rad(u(56));
q3d=deg2rad(u(57));

Qd = [q1d;q2d;q3d];

%Desired joint velocity
q1dp=deg2rad(u(58));
q2dp=deg2rad(u(59));
q3dp=deg2rad(u(60));

Qdp = [q1dp;q2dp;q3dp];

%Joint Position Vector
Q=[q1; q2; q3];

%Joint Velocity Vector
Qp=[qp1; qp2; qp3];

% Robot Base
[Hts,~,~]=Hts_J_MCG(qp1,qp2,qp3,q1,q2,q3,0,0,0,L1,L2,L3,L4,L5,L6,L7,L8,L9,L10,zeros(18,1),0,0,0,0);

T0_W=[-1 0 0 0;0 -1 0 0;0 0 1 0;0 0 0 1];

% Homogeneous Transformations

T1_0=Hts(:,:,1);

T2_1=Hts(:,:,2);

T3_2=Hts(:,:,3);

T2_0=T1_0*T2_1;
T3_0=T2_0*T3_2;

T1_W=T0_W*T1_0;
T2_W=T0_W*T2_0;
T3_W=T0_W*T3_0;


Xef_W = T3_W(1:3,4);

sampleTime=0.06;
if t==0
    close all;
    %% TF publisher
    tftree = rostf;
    tfStampedMsg = rosmessage('geometry_msgs/TransformStamped');
    tfStampedMsg.Header.FrameId = 'world';
    tfStampedMsg.ChildFrameId = 'DH_0';
    
    tfStampedMsg1 = rosmessage('geometry_msgs/TransformStamped');
    tfStampedMsg1.Header.FrameId = 'world';
    tfStampedMsg1.ChildFrameId = 'DH_1';
    
    tfStampedMsg2 = rosmessage('geometry_msgs/TransformStamped');
    tfStampedMsg2.Header.FrameId = 'world';
    tfStampedMsg2.ChildFrameId = 'DH_2';
    
    tfStampedMsg3 = rosmessage('geometry_msgs/TransformStamped');
    tfStampedMsg3.Header.FrameId = 'world';
    tfStampedMsg3.ChildFrameId = 'DH_3';

    
    %% Joint State Publisher
    %Use here the correct topic name --see bringup launch file--
    jointpub = rospublisher('/ursa_joint_states', 'sensor_msgs/JointState');
    jointmsg = rosmessage(jointpub);
    
    % specific names of the joints --see urdf file--
    jointmsg.Name={ 'ursa_shoulder_pan_joint', 'ursa_shoulder_lift_joint', 'ursa_elbow_joint', 'ursa_wrist_1_joint', 'ursa_wrist_2_joint', 'ursa_wrist_3_joint'};
    
    for i=1:6
        jointmsg.Velocity(i)=0.0;
        jointmsg.Effort(i)=0.0;
    end
    
    counter=0;
    
    dt = str2double(get_param('DSimulator_robot3GDL','FixedStep'));
    t_end = str2double(get_param('DSimulator_robot3GDL','StopTime'));
    %t_end = 10;
    error=zeros(round(t_end*sampleTime/dt),1);
    
end

%% JOINT STATE MSG and TF MSG

if(~mod(t,sampleTime))
    rT=rostime('now');
    jointmsg.Header.Stamp=rT;
    jointmsg.Header.Seq=counter;
    offsets = [0;-pi/2;0;-pi/2;0;0];
    Q_all = [q1;q2;q3;0;0;0];
    jointmsg.Position=Q_all+offsets;
    send(jointpub,jointmsg);
    
    
    getTF(tfStampedMsg, T0_W, counter, rT);
    getTF(tfStampedMsg1, T1_W, counter, rT);
    getTF(tfStampedMsg2, T2_W, counter, rT);
    getTF(tfStampedMsg3, T3_W, counter, rT);
    
    arrayTFs=[tfStampedMsg;
        tfStampedMsg1;
        tfStampedMsg2;
        tfStampedMsg3];
    
    
    counter=counter+1;
    
    sendTransform(tftree, arrayTFs);
   
end

%%
%disp("End-effector position")
%disp(Xef_W)

Xout=[Xef_W];




end

