%% DQN function to learn the optimal policy for the cartpole task
% training and simulation in the test runs have a time step dt = 0.1
% NN is trained each iteration for one episode by 200 random samples from
%   the replay buffer D
% Early stopping is achieved when the test runs return a cumulative reward
%   below -20 for 10 consecutive runs
% Training was executed on a GTX1070 Ti and takes ~2.5h to converge
%  (-> triggering the stopping condition)


function [accumulated_reward,num_iters,Qnet,D] = DQN0()
tic
%% Initialize parameters and matrices
%constants
m_p = 0.5;
m_c = 0.5;
l = 0.6;
g = 9.82;
b = 0.1;
force = 10;

x_limit = 6;
d_x_limit = 10;
d_theta_limit = 10;
r_best = -60;
dt = 0.1;              % Time step
x_init = 0;
d_x_init=0;
theta_init = pi;
d_theta_init = 0;           
gamma = 0.99;
n_train = 400;
Iters = 500;

a_density = 8;
as = linspace(-force,force,a_density);          %descrete number of selectable actions
D_size = 5;
epsilon_reward = -Iters;
best_reward = epsilon_reward;
last_rewards = -Iters*ones(10,1);
count_last_rewards = 0;
N = 200;
%initialize net
netconf = [120,200,60];
Qnet = feedforwardnet(netconf);
Qnet.trainFcn = 'trainscg';
Qnet.trainParam.epochs = 1;
Qnet.trainParam.showWindow = false;

D = zeros(D_size*Iters,6);
counter_max = D_size*Iters;
init_D()

D_sample = datasample(D,N,1,'Replace',false);
train_init_samples = D_sample(:,1:5);
train_init_targets = D_sample(:,6);

Qnet = train(Qnet,train_init_samples',train_init_targets.','useGPU','yes'); 
Qnet_save = Qnet;
accumulated_reward = zeros(n_train,1);
num_iters = zeros(n_train,1);
num_iters_train = zeros(n_train,1);
epsilon_thresh = zeros(n_train,1);
counter = 0;
count_best = 0;
for i=1:n_train
    x = x_init;
    d_x = d_x_init;
    theta = theta_init;
    d_theta = d_theta_init;
    
    br = false;
    epsilon = rand(1);
    fprintf("Start epoch #%d with epsilon thresh = %f\n\n",i,mean(last_rewards)/epsilon_reward);
    if epsilon <= mean(last_rewards)/epsilon_reward
        %exploration
        random_pick = randi([1,a_density]);
        a = as(random_pick);
    else
        %exploitation
        q_sample = zeros(a_density,1);
        for k=1:a_density
           a_sample = as(k);
           q_sample(k) = Qnet_save([x;d_x;theta;d_theta;a_sample]);
        end
        [~,a_select] = max(q_sample);
        a = as(a_select);
    end
    for j=1:Iters
        
            epsilon = rand(1);
            if epsilon <= mean(last_rewards)/epsilon_reward
                %exploration
                random_pick = randi([1,a_density]);
                a = as(random_pick);
            else
                %exploitation
                q_sample = zeros(a_density,1);
                for k=1:a_density
                    a_sample = as(k);
                    q_sample(k) = Qnet_save([x;d_x;theta;d_theta;a_sample]);
                end
                [~,a_select] = max(q_sample);
                a = as(a_select);
            end
        
        %sim_cartpole(x,x_next,theta,theta_next,a)
        %for j1 = 1:10
            [dd_theta, dd_x] = dyn_equation(m_p, m_c, g, l, theta, d_theta, d_x, a, b);
            [theta_next, d_theta_next, x_next, d_x_next] = Euler(dd_theta, d_theta, theta, dd_x, d_x, x, dt);
            
            if abs(x_next) > x_limit
                fprintf("x-limit reached at step: %d\n\n", j);
                break;
            end
            counter = counter + 1;    
            %Receive the reward of the next state
            r = get_r();
            qn = zeros(a_density,1);
            for k=1:a_density
                anext = as(k);
                qn(k) = Qnet_save([x_next;d_x_next;theta_next;d_theta_next;anext]);
            end
            Qmax = max(qn);

            if mod(counter,100) == 0
                %fprintf("Qmax: %f, Counter: %d\n\n",Qmax, counter)
                Qnet_save = Qnet;
            end
            
            q = r + gamma*Qmax;
            
            D(counter,:) = [x,d_x,theta,d_theta,a,q];
            D_sample = datasample(D,N,1,'Replace',false);
            train_samples = D_sample(:,1:5);
            train_targets = D_sample(:,6);
            
            Qnet = train(Qnet,train_samples',train_targets.','useGPU','yes');
            
         
        
            x = x_next;
            d_x = d_x_next;
            theta = theta_next;
            d_theta = d_theta_next;
            if counter == counter_max
                counter = 0;
            end
        
    end
    num_iters_train(i) = j;
    epsilon_thresh(i) = mean(last_rewards)/epsilon_reward;
    br=false;
    if mod(i,20)==0
        [r_acc,j_acc] = test_Qnet_sim();
        accumulated_reward(i) = r_acc;
        num_iters(i) = j_acc;
        
    else
        [r_acc,j_acc] = test_Qnet();
        accumulated_reward(i) = r_acc;
        num_iters(i) = j_acc;
    end
    if j_acc == Iters
        count_last_rewards = count_last_rewards + 1;
        last_rewards(count_last_rewards) = r_acc;
        if count_last_rewards == 10
            count_last_rewards = 0;
        end
    end
        
    if j_acc == Iters && r_acc > -40
        count_best = count_best +1;
        disp(count_best)
    else
        count_best = 0;
    end
    if count_best >= 10
        time = toc;
        disp(time)
        save Qnet
        break;
    end
end
time = toc;
function r = get_r()
    f_cos = 2;
    j_current = [0.5*x^2,sin(theta),f_cos*cos(theta)];
    j_target = [0,0,f_cos];
    A=1;
    T_inv = A^2*[1,l,0;l,l^2,0;0,0,l^2];
    r = -(1-exp(-0.5*(j_current-j_target)*T_inv*(j_current-j_target)'));
end   

function init_D()
    c=0;
    while c <= counter_max
        x = x_init;
        d_x = d_x_init;
        theta = theta_init;
        d_theta = d_theta_init;
        a = as(randi([1,a_density]));
        for u = 1:Iters
            
                a = as(randi([1,a_density]));
            
                [dd_theta, dd_x] = dyn_equation(m_p, m_c, g, l, theta, d_theta, d_x, a, b);
                [theta_next, d_theta_next, x_next, d_x_next] = Euler(dd_theta, d_theta, theta, dd_x, d_x, x, dt);
                if abs(x_next) > x_limit
                    disp(u)
                    break;
                elseif c > counter_max
                    disp("finished initialization of D")
                    break;
                end
                c = c + 1; 
                %Receive the reward of the next state
                r = get_r();
                q = r;
                D(c,:) = [x,d_x,theta,d_theta,a,q];
            x = x_next;
            d_x = d_x_next;
            theta = theta_next;
            d_theta = d_theta_next;
        end
        if c > counter_max
            break;
        end
    end
end

function [r_acc,w] = test_Qnet_sim()
    x = x_init;
    d_x = d_x_init;
    theta = theta_init;
    d_theta = d_theta_init;
    r_acc = 0;
    br = false;
    q_sample = zeros(a_density,1);
    for w1=1:a_density
        a_sample = as(w1);
        q_sample(w1) = Qnet_save([x;d_x;theta;d_theta;a_sample]);
    end
    [~,a_select] = max(q_sample);
    a = as(a_select);
    for w = 1:Iters
        
            q_sample = zeros(a_density,1);
            for w1=1:a_density
                a_sample = as(w1);
                q_sample(w1) = Qnet_save([x;d_x;theta;d_theta;a_sample]);
            end
            [~,a_select] = max(q_sample);
            a = as(a_select);
            %for w1=1:10
            [dd_theta, dd_x] = dyn_equation(m_p, m_c, g, l, theta, d_theta, d_x, a, b);
            [theta_next, d_theta_next, x_next, d_x_next] = Euler(dd_theta, d_theta, theta, dd_x, d_x, x, dt);
            %Receive the reward of the next state
            if abs(x_next) > x_limit
                br = true;
                break;
            end
            r = get_r();
            r_acc = r_acc + r;
            sim_cartpole(x,x_next,theta,theta_next,a)
        
        x = x_next;
        d_x = d_x_next;
        theta = theta_next;
        d_theta = d_theta_next;
        %end
        
    end
    
    if w == Iters
        if r_acc > best_reward
            best_reward = r_acc;
        end
        if r_acc > r_best
            save("Qnet"+i+".mat","Qnet_save")
            r_best = r_acc;
        end
    else
        fprintf("Failed at step: %d\n\n", w)
    end
    fprintf("Reward: %f\n\n",r_acc)
end  
function [r_acc,w] = test_Qnet()
    x = x_init;
    d_x = d_x_init;
    theta = theta_init;
    d_theta = d_theta_init;
    r_acc = 0;
    br = false;
    q_sample = zeros(a_density,1);
    for w1=1:a_density
        a_sample = as(w1);
        q_sample(w1) = Qnet_save([x;d_x;theta;d_theta;a_sample]);
    end
    [~,a_select] = max(q_sample);
    a = as(a_select);
    for w = 1:Iters
       
            q_sample = zeros(a_density,1);
            for w1=1:a_density
                a_sample = as(w1);
                q_sample(w1) = Qnet_save([x;d_x;theta;d_theta;a_sample]);
            end
            [~,a_select] = max(q_sample);
            a = as(a_select);
            %for w1=1:10
            [dd_theta, dd_x] = dyn_equation(m_p, m_c, g, l, theta, d_theta, d_x, a, b);
            [theta_next, d_theta_next, x_next, d_x_next] = Euler(dd_theta, d_theta, theta, dd_x, d_x, x, dt);
            %Receive the reward of the next state
            if abs(x_next) > x_limit
                br = true;
                break;
            end
            r = get_r();
            r_acc = r_acc + r;
            %sim_cartpole(x,x_next,theta,theta_next,a)
        
        x = x_next;
        d_x = d_x_next;
        theta = theta_next;
        d_theta = d_theta_next;
        %end
        
    end
    
    if w == Iters
        if r_acc > best_reward
            best_reward = r_acc;
        end
        if r_acc > r_best
            save("Qnet"+i+".mat","Qnet_save")
            r_best = r_acc;
        end
    else
        fprintf("Failed at step: %d\n\n", w)
    end
    fprintf("Reward: %f\n\n",r_acc)
end

end

