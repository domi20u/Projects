%% Simulation of the Qnets according to task specification and original reward function
%the reward is only calculated every 0.1s

function [r_acc, x_acc,theta_acc,a_acc] = sim2_Qnet(Qnet)
    pause on;
    m_p = 0.5;
    m_c = 0.5;
    l = 0.6;
    g = 9.82;
    b = 0.1;
    force = 10;

    x_limit = 6;
    d_x_limit = 10;
    d_theta_limit = 10;
    dt = 0.1;        
    sim_dt = 0.01;
    
    a_density = 8;
    as = linspace(-force,force,a_density);
    x = 0;
    d_x = 0;
    theta = pi;
    d_theta = 0;
    r_acc = 0;
    br = false;
    iters=50;
    sim_steps = iters*10;
    
    x_acc = zeros(sim_steps,1);
    theta_acc = zeros(sim_steps,1);
    a_acc = zeros(sim_steps,1);
    q_sample = zeros(a_density,1);
    for w1=1:a_density
        a_sample = as(w1);
        q_sample(w1) = Qnet([x;d_x;theta;d_theta;a_sample]);
    end
    [~,a_select] = max(q_sample);
    a = as(a_select);
    for w = 1:iters
        if mod(w,10)==0
            disp(w*10)
        end
        if mod(w,1)==0
            q_sample = zeros(a_density,1);
            for w1=1:a_density
                a_sample = as(w1);
                q_sample(w1) = Qnet([x;d_x;theta;d_theta;a_sample]);
            end
            [~,a_select] = max(q_sample);
            a = as(a_select);
            
        end
        %if(w<10)
            %disp(w+(w-1)*9)
        %end
        x_acc(w+(w-1)*9) = x;
        theta_acc(w+(w-1)*9) = theta;
        a_acc(w+(w-1)*9) = a;
        sim_x = x;
        sim_theta = theta;
        sim_d_theta = d_theta;
        sim_d_x = d_x;
        for w1=1:9
            [sim_dd_theta, sim_dd_x] = dyn_equation(m_p, m_c, g, l, sim_theta, sim_d_theta, sim_d_x, a, b);
            [sim_theta, sim_d_theta, sim_x, sim_d_x] = Euler(sim_dd_theta, sim_d_theta, sim_theta, sim_dd_x, sim_d_x, sim_x, sim_dt);
            x_acc(w+(w-1)*9+w1) = sim_x;
            theta_acc(w+(w-1)*9+w1) = sim_theta;
            a_acc(w+(w-1)*9+w1) = a;
        end
        
        [dd_theta, dd_x] = dyn_equation(m_p, m_c, g, l, theta, d_theta, d_x, a, b);
        [theta_next, d_theta_next, x_next, d_x_next] = Euler(dd_theta, d_theta, theta, dd_x, d_x, x, dt);
        %if w<10
            %disp([a,x,theta])
        %end
        %Receive the reward of the next state
        if abs(x_next) > x_limit
            br = true;
            disp("x out of range")
            break;
        end
        
        r = get_r();
        r_acc = r_acc + r;
        %sim_cartpole(x,x_next,theta,theta_next,a)
        x = x_next;
        d_x = d_x_next;
        theta = theta_next;
        d_theta = d_theta_next;
        
    end
    disp("Reward:")
    disp(r_acc)
    %disp(br)
    %disp(w)
    %disp(x_acc(1:100))
    %for w = 1:iters*10
        
        %sim_cartpole(x_acc(w),0,theta_acc(w),0,a_acc(w))
        %pause(0.01)
    %end
    
    cartpole_simulation(x_acc,theta_acc,a_acc)
    
    function r = get_r()
        j_current = [x,sin(theta),cos(theta)];
        j_target = [0,0,1];
        A=1;
        T_inv = A^2*[1,l,0;l,l^2,0;0,0,l^2];
        r = -(1-exp(-0.5*(j_current-j_target)*T_inv*(j_current-j_target)'));
    end   
    
end

