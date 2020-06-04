function symModel()

syms q1 q2 q3 L1 L2 L3 L4 L5 L6 L7 L8 L9 L10 qp1 qp2 qp3 m1 m2 m3 g gx gy gz real
syms I111 I112 I113 I122 I123 I133 I211 I212 I213 I222 I223 I233 I311 I312 I313 I322 I323 I333 I1 I2 I3 real

Q = [q1;q2;q3];
Qp = [qp1;qp2;qp3];
I1 = [I111,I112,I113;I112,I122,I123;I113,I123,I133];
I2 = [I211,I212,I213;I212,I222,I223;I213,I223,I233];
I3 = [I311,I312,I313;I312,I322,I323;I313,I323,I333];

fid=fopen('Hts_J_MCG.m','w');
fprintf(fid,'function [Hts,J,MCG]=Hts_J_MCG(qp1,qp2,qp3,q1,q2,q3,m1,m2,m3,L1,L2,L3,L4,L5,L6,L7,L8,L9,L10,I,g,gx,gy,gz)\n\n');

fprintf(fid,'I111=I(1);\n');
fprintf(fid,'I112=I(2);\n');
fprintf(fid,'I113=I(3);\n');
fprintf(fid,'I122=I(4);\n');
fprintf(fid,'I123=I(5);\n');
fprintf(fid,'I133=I(6);\n');

fprintf(fid,'I211=I(7);\n');
fprintf(fid,'I212=I(8);\n');
fprintf(fid,'I213=I(9);\n');
fprintf(fid,'I222=I(10);\n');
fprintf(fid,'I223=I(11);\n');
fprintf(fid,'I233=I(12);\n');

fprintf(fid,'I311=I(13);\n');
fprintf(fid,'I312=I(14);\n');
fprintf(fid,'I313=I(15);\n');
fprintf(fid,'I322=I(16);\n');
fprintf(fid,'I323=I(17);\n');
fprintf(fid,'I333=I(18);\n');

x=0;
y=0;
z=0;
T0_W = [1 0 0 x; 0 1 0 y; 0 0 1 z; 0 0 0 1];
Rx0_W= [1     0          0       0   ;
        0 cos(0) -sin(0)   0   ;
        0 sin(0)  cos(0)   0   ;
        0     0          0       1   ];  
Rz0_W= [cos(0) -sin(0)   0 0  ;
        sin(0)  cos(0)   0 0  ;
        0           0          1 0   ;
        0           0          0 1   ]; 

H0_W = T0_W * Rx0_W * Rz0_W;

D=[q1 L1 0 pi/2;
    q2+pi/2 0 L3 0;
    q3 L2+L4 L5 0];
sD=size(D);

for i=1:sD(1)
    Rz= [cos(D(i,1)) -sin(D(i,1)) 0   0   ;
        sin(D(i,1))  cos(D(i,1)) 0   0   ;
        0            0       1   0   ;
        0            0       0   1   ];
    
    Tz= [    1            0       0   0   ;
        0            1       0   0   ;
        0            0       1 D(i,2);
        0            0       0   1   ];
    
    Tx= [    1            0       0 D(i,3);
        0            1       0   0   ;
        0            0       1   0   ;
        0            0       0   1   ];
    
    Rx= [1     0             0        0   ;
        0 cos(D(i,4)) -sin(D(i,4))   0   ;
        0 sin(D(i,4))  cos(D(i,4))   0   ;
        0     0             0        1   ];
    
    relHt{i}=Rz*Tz*Tx*Rx;
end

D=[q1 L6 0 0;
    q2+pi/2 L7 L8 0;
    q3 L9 L10 0];
sD=size(D);

for i=1:sD(1)
    Rz= [cos(D(i,1)) -sin(D(i,1)) 0   0   ;
        sin(D(i,1))  cos(D(i,1)) 0   0   ;
        0            0       1   0   ;
        0            0       0   1   ];
    
    Tz= [    1            0       0   0   ;
        0            1       0   0   ;
        0            0       1 D(i,2);
        0            0       0   1   ];
    
    Tx= [    1            0       0 D(i,3);
        0            1       0   0   ;
        0            0       1   0   ;
        0            0       0   1   ];
    
    Rx= [1     0             0        0   ;
        0 cos(D(i,4)) -sin(D(i,4))   0   ;
        0 sin(D(i,4))  cos(D(i,4))   0   ;
        0     0             0        1   ];
    
    relHt_cm{i}=Rz*Tz*Tx*Rx;
end


Ht{1} = relHt{1}
Ht{2} = relHt{1}*relHt{2}
Ht{3} = relHt{1}*relHt{2}*relHt{3}
Ht_cm{1}=relHt_cm{1}
Ht_cm{2}=relHt{1}*relHt_cm{2}
Ht_cm{3}=relHt{1}*relHt{2}*relHt_cm{3}

Ht{1}
Ht{2}
Ht{3}
relHt_W{1}=Ht{1}
relHt_W{2}=H0_W*relHt{2}
relHt_W{3}=H0_W*relHt{2}


for k=1:3
    for i=1:4
        for j=1:4
            fprintf(fid,'H%d_%d(%d,%d)=%s;\n',k,k-1,i,j,char(relHt{k}(i,j)));
        end
    end
end
fprintf(fid,'\n');

for k=1:3
    for i=1:4
        for j=1:4
            fprintf(fid,'Hcm%d_%d(%d,%d)=%s;\n',k,k-1,i,j,char(relHt_cm{k}(i,j)));
        end
    end
end
fprintf(fid,'\n');

t3_0 = Ht{3}(1:3,4);
tcm3_0=Ht_cm{3}(1:3,4);
tcm2_0=Ht_cm{2}(1:3,4);
tcm1_0=Ht_cm{1}(1:3,4);

Jv = [diff(t3_0,q1)*qp1 diff(t3_0,q2)*qp2 diff(t3_0,q3)*qp3];
Jv_cm1 = [diff(tcm1_0,q1)*qp1 diff(tcm1_0,q2)*qp2 diff(tcm1_0,q3)*qp3];
Jv_cm2 = [diff(tcm2_0,q1)*qp1 diff(tcm2_0,q2)*qp2 diff(tcm2_0,q3)*qp3];
Jv_cm3 = [diff(tcm3_0,q1)*qp1 diff(tcm3_0,q2)*qp2 diff(tcm3_0,q3)*qp3];

r3_0 = [0;-q2+q3;q1];
r2_0 = [0;-q2;q1];
r1_0 = [0;0;q1];
Jw = [diff(r3_0,q1)*qp1 diff(r3_0,q2)*qp2 diff(r3_0,q3)*qp3];
Jw_cm3=Jw;
Jw_cm2=[diff(r2_0,q1)*qp1 diff(r2_0,q2)*qp2 diff(r2_0,q3)*qp3];
Jw_cm1=[diff(r1_0,q1)*qp1 diff(r1_0,q2)*qp2 diff(r1_0,q3)*qp3];
Jef_0=[Jv;Jw];
Jef = sym(zeros(6,3));
Jef(1:3,1) = simplify(cross([0;0;1],Ht{3}(1:3,4)));
Jef(4:6,1) = [0;0;1];
Jef(1:3,2) = simplify(cross(Ht{1}(1:3,3),Ht{3}(1:3,4)-Ht{1}(1:3,4)));
Jef(4:6,2) = Ht{1}(1:3,3);
Jef(1:3,3) = simplify(cross(Ht{2}(1:3,3),Ht{3}(1:3,4)-Ht{2}(1:3,4)));
Jef(4:6,3) = Ht{2}(1:3,3);

Jefp = simplify(diff(Jef,q1)*qp1+diff(Jef,q2)*qp2+diff(Jef,q3)*qp3);


% jacobian of center of mass 1
Jv_cm1 = sym(zeros(3,3));
Jw_cm1 = sym(zeros(3,3));
Jv_cm1(:,1) = simplify(cross([0;0;1],Ht_cm{1}(1:3,4)));
Jw_cm1(:,1) = [0;0;1];

% jacobian of center of mass 2
Jv_cm2 = sym(zeros(3,3));
Jw_cm2 = sym(zeros(3,3));
Jv_cm2(:,1) = simplify(cross([0;0;1],Ht_cm{2}(1:3,4)));
Jw_cm2(:,1) = [0;0;1];
Jv_cm2(:,2) = simplify(cross(Ht{1}(1:3,3),(Ht_cm{2}(1:3,4)-Ht{1}(1:3,4))));
Jw_cm2(:,2) = Ht{1}(1:3,3);

% jacobian of center of mass 3
Jv_cm3 = sym(zeros(3,3));
Jw_cm3 = sym(zeros(3,3));
Jv_cm3(:,1) = simplify(cross([0;0;1],Ht_cm{3}(1:3,4)));
Jw_cm3(:,1) = [0;0;1];
Jv_cm3(:,2) = simplify(cross(Ht{1}(1:3,3),(Ht_cm{3}(1:3,4)-Ht{1}(1:3,4))));
Jw_cm3(:,2) = Ht{1}(1:3,3);
Jv_cm3(:,3) = simplify(cross(Ht{2}(1:3,3),(Ht_cm{3}(1:3,4)-Ht{2}(1:3,4))));
Jw_cm3(:,3) = Ht{2}(1:3,3);


for i=1:6
    for j=1:3
        fprintf(fid,'Jef(%d,%d)=%s;\n',i,j,char(Jef(i,j)));
    end
end
fprintf(fid,'\n'); 

for i = 1:6
    for j = 1:3
        fprintf(fid,'Jefp(%d,%d)=%s;\n',i,j,char(Jefp(i,j)));
    end 
end
%for i=1:6
%    for j=1:3
%        fprintf(fid,'Jef_0(%d,%d)=%s;\n',i,j,char(Jef_0(i,j)));
%    end
%end
fprintf(fid,'\n');

%Calc of M matrix
M=simplify(m1.*Jv_cm1'*Jv_cm1 + Jw_cm1'*Ht_cm{1}(1:3,1:3)*I1*Ht_cm{1}(1:3,1:3)'*Jw_cm1);
M=M+simplify(m2.*Jv_cm2'*Jv_cm2 + Jw_cm2'*Ht_cm{2}(1:3,1:3)*I2*Ht_cm{2}(1:3,1:3)'*Jw_cm2);
M=M+simplify(m3.*Jv_cm3'*Jv_cm3 + Jw_cm3'*Ht_cm{3}(1:3,1:3)*I3*Ht_cm{3}(1:3,1:3)'*Jw_cm3);

for i=1:3
    for j=1:3
        fprintf(fid,'M(%d,%d)=%s;\n',i,j,char(M(i,j)));
    end
end
fprintf(fid,'\n'); 

%Calc of C matrix
for k=1:3
    for j=1:3
        c=0;
        for i=1:3
            c=c+(diff(M(k,j),Q(i))+diff(M(k,i),Q(j))-diff(M(i,j),Q(k)))*Qp(i);
        end
        C(k,j)=0.5*simplify(c);
    end
end

for i=1:3
    for j=1:3
        fprintf(fid,'C(%d,%d)=%s;\n',i,j,char(C(i,j)));
    end
end
fprintf(fid,'\n');

%Calc of G matrix
P_cm1 = m1*g*[gx,gy,gz]*tcm1_0;
P_cm2 = m2*g*[gx,gy,gz]*tcm2_0;
P_cm3 = m3*g*[gx,gy,gz]*tcm3_0;
P = simplify(P_cm1 + P_cm2 + P_cm3)
G(1,1) = simplify(diff(P,q1))
G(2,1) = simplify(diff(P,q2))
G(3,1) = simplify(diff(P,q3))

fprintf(fid,'G(1,1)=%s;\n',char(G(1,1)));
fprintf(fid,'G(2,1)=%s;\n',char(G(2,1)));
fprintf(fid,'G(3,1)=%s;\n',char(G(3,1)));
fprintf(fid,'\n');

fprintf(fid,'Hts(:,:,1)=H1_0;\n');
fprintf(fid,'Hts(:,:,2)=H2_1;\n');
fprintf(fid,'Hts(:,:,3)=H3_2;\n');
fprintf(fid,'Hts(:,:,4)=Hcm1_0;\n');
fprintf(fid,'Hts(:,:,5)=Hcm2_1;\n');
fprintf(fid,'Hts(:,:,6)=Hcm3_2;\n');
fprintf(fid,'J{1}=Jef;\n');
fprintf(fid,'J{2}=Jefp;\n');
fprintf(fid,'MCG{1}=M;\n');
fprintf(fid,'MCG{2}=C;\n');
fprintf(fid,'MCG{3}=G;\n');





end

