classdef dmp
    %Train and optimize 2D-trajectories with dmps
    %Mandatory input: text-file with a trajectory to be imitated by dmp
    %with time and desired positions, velocities and acceleration per time
    %step
    
    %Methods to call 
    %train: learn mean_x and mean_y for the given trajectory
    %optimize: learn desired trajectory with learned initial mean_x and mean_y 
    
    %Necessary function files:
    %rbfn_fit.m
    %rbfn_predict.m
    %exponential_fcn.m
    %dE_spring.m
    
    %Examples
    %test.m: optimizing the throwing task
    %dmp_pp_test.m: test the rotation and dilatation invariance according
    %to the dmp++ paper
    
    properties
        
        damping_coefficient     %input for dmp
        spring_constant         %input for dmp, will be computed such that the system is critically damped
        mass                    %input for dmp
        alpha                   %for computing the exponential phase xs_phase (same for x and y-direction)
        xs_phase                %exponential input for the function approximation
        n_rbf                   %Number of basis functions for th function approximation
        bf_type                 %Basis function type: default is "mollifier", change to anything else for using radial basis functions
        centers                 %Basis function centers for n_rbf [n_rbf,1]
        widths                  %Basis function widths (differently computed for mollifier and rbf)
        mean_x                  %Parameters [n_rbf x 1] for approximating the forcing terms in x-direction
        mean_y                  %Parameters [n_rbf x 1] for approximating the forcing terms in y-direction
        covar_x                 %Parameters [n_rbf x n_rbf] for the exploration of mean_x, starting at sigma and decaying according to covar_decay_factor (select sigma in dmp(..) and decay factor in optimize(...)) 
        covar_y                 %Parameters [n_rbf x n_rbf] for the exploration of mean_y
        x_init                  %Initial point [x,y] of the trajectory
        x_end                   %Final point [x,y] of the trajectory
        tau                     %Duration of the trajectory execution, constant, according to imitated trajectory: t(end)-t(1)
        n_time_steps            %Selectable number of time steps to execute the trajectory
        learned_position        %Constant value initialized from imitating the original trajectory: x_end-x_init
        rescale                 %Boolean, initialized to 1, keeps the trajectory invariant to dilatation and rotation when selecting different initial or goal positions
        
    end
    
    methods
        function obj = dmp(alpha,damping_coefficient,mass,n_rbf,sigma,n_time_steps)
            %DMP Construct an instance of this class
            obj.alpha = alpha;
            obj.damping_coefficient = damping_coefficient;
            obj.spring_constant = 1/4*damping_coefficient^2;
            obj.mass = mass;
            obj.n_rbf = n_rbf;
            obj.covar_x = sigma*sigma*eye(n_rbf);
            obj.covar_y = sigma*sigma*eye(n_rbf);
            obj.n_time_steps = n_time_steps;
            obj.bf_type = "mollifier"; % else "rbf"
            obj.rescale = 1;
        end
        
        function obj = train(obj,trajectory_file)
            %Imitate the desired trajectory from the trajectory_file with a dmp 
            trajectory = readmatrix(trajectory_file);
            obj.tau = trajectory(end,1)-trajectory(1,1);
            xs_traj = trajectory(:,2:3);
            xds_traj = trajectory(:,4:5);
            xdds_traj = trajectory(:,6:7);
            obj.x_init = xs_traj(1,:);
            obj.x_end = xs_traj(end,:);
            obj.learned_position = obj.x_end-obj.x_init;
            [obj.xs_phase,~] = exponential_fcn(obj.alpha,obj.tau,obj.n_time_steps,1,0);
            f_target = obj.tau*obj.tau*xdds_traj + (obj.spring_constant*(xs_traj - obj.x_end) + obj.damping_coefficient*obj.tau*xds_traj)/obj.mass;
            [obj.mean_x,obj.centers,obj.widths,activation_x] = rbfn_fit(obj.n_rbf,f_target(:,1),obj.bf_type,obj.tau);
            [obj.mean_y,~,~,activation_y] = rbfn_fit(obj.n_rbf,f_target(:,2),obj.bf_type,obj.tau);
            figure('Position',[50 50 1000 800])
            subplot(2,1,1)
            sgtitle("Reproducing the forcing terms in X,Y-Direction")
            hold on
            weighted_ac = obj.mean_x'.*activation_x;
            plot(f_target(:,1),'b','LineWidth',2)
            plot(sum(weighted_ac,2),'k','LineWidth',2)
            plot(weighted_ac,'k:')
            xlabel("Time Steps")
            ylabel("Forcing Terms in X")
            legend("Demonstration","Reproduction","Basis Functions")
            subplot(2,1,2)
            hold on
            weighted_ac = obj.mean_y'.*activation_y;
            plot(f_target(:,2),'b','LineWidth',2)
            plot(sum(weighted_ac,2),'k','LineWidth',2)
            plot(weighted_ac,'k:')
            xlabel("Time Steps")
            ylabel("Forcing Terms in Y")
        end
        
        function [obj,full_costs] = optimize(obj,max_runs,n_samples,h,covar_decay_factor,saving,directory)
            obj.plot_rollout(0,"r",2);
            %cost_avg = zeros(max_runs,3);            
            for ii = 1:max_runs
                mean_expl = explore(obj,n_samples);
                meanX = mean_expl(:,1:obj.n_rbf);
                meanY = mean_expl(:,obj.n_rbf+1:end);
                costs = zeros(n_samples,3);
                if saving
                    if ~exist(directory, 'dir')
                        mkdir(directory)
                    end
                    mkdir(directory+"/run_"+ii)
                end
                for jj = 1:n_samples
                    
                    [~,ydd,y_ball,~] = dynamics(obj,meanX(jj,:),meanY(jj,:),saving,directory+"/run_"+ii+"/rollout_sample_"+jj+".txt");
                    costs(jj,:) = evaluate_rollout(obj,ydd,y_ball);
                end
                weights = costs_to_weights(obj,costs(:,1),h);
                mean_new = mean(mean_expl.*weights)/mean(weights);
                obj.mean_x = mean_new(:,1:obj.n_rbf);
                obj.mean_y = mean_new(:,obj.n_rbf+1:end);
                obj.covar_x = covar_decay_factor^2*obj.covar_x;
                obj.covar_y = covar_decay_factor^2*obj.covar_y;
                if saving
                    writematrix(mean_new,directory+"/run_"+ii+"/new_mean.txt");
                end
                cost_avg(ii,:) = mean(costs);
                %if cost_avg(ii,1) < 0.05
                    %break
                %end
                if ii == max_runs
                    obj.plot_rollout(0,"b",2);
                else
                    obj.plot_rollout(0);
                end
            end
            
            %figure
            %plot(cost_avg)
            full_costs = cost_avg;%(:,1);
        end
        
        function [mean] = explore(obj,n_samples)
            mean_x_expl = mvnrnd(obj.mean_x,obj.covar_x,n_samples); %samples from distribution
            mean_y_expl = mvnrnd(obj.mean_y,obj.covar_y,n_samples);
            mean = [mean_x_expl,mean_y_expl];
        end
        
        function costs = evaluate_rollout(obj,ydd,y_ball)
            acc_weight = 0.001;
            x_goal = -0.7;
            x_margin = 0.01;
            T = length(ydd);
            dist_to_landing_site = abs(y_ball(end,1)-x_goal)-x_margin;
            if dist_to_landing_site < 0.0
                dist_to_landing_site = 0.0;
            end
            sum_ydd = sum(sum(ydd.^2));
            costs = zeros(3,1);
            costs(2) = dist_to_landing_site;
            costs(3) = acc_weight * sum_ydd / T;
            costs(1) = costs(2)+costs(3);
        end

        function weights = costs_to_weights(obj,full_costs,h)
            %Compute weigts to update the mean_x and mean_y of a dmp
            costs_range = max(full_costs)-min(full_costs);
            weights = ones(length(full_costs),1);
            if costs_range == 0
                weights = ones(length(full_costs),1);
            else
                weigths = exp(-h*(full_costs-min(full_costs))/costs_range);
            end
            weights = weigths/sum(weights);
        end 
        
        function [y,ydd,y_ball,release_point] = dynamics(obj,meanX,meanY,saving,file_out)
            %Compute the dynamics (y,yd,ydd) from mean_x and mean_y
            [out1,~] = rbfn_predict(obj.xs_phase,meanX,obj.centers,obj.widths,obj.bf_type);
            [out2,~] = rbfn_predict(obj.xs_phase,meanY,obj.centers,obj.widths,obj.bf_type);
            forcing_terms = [out1',out2'];
            if obj.rescale
                new_position = obj.x_end - obj.x_init;
                obj.learned_position;
                M = rotodilatation(obj,obj.learned_position,new_position);
                forcing_terms = forcing_terms*M;
            end
            forcing_terms_flip = flip(forcing_terms);
            %other xs, xds values for SPRING by integration
            xs_spring = zeros(obj.n_time_steps,4); %2D for Y and Z parts of first order diff eq
            xds_spring = zeros(obj.n_time_steps,4);
            xs_spring(1,1:2) = obj.x_init; %see integrateStart: x.segment(0) = initial_state
            xs_spring(1,3:4) = zeros(1,2);

            [yd_,zd_] = dE_spring(xs_spring(1,:),obj.tau,obj.spring_constant,obj.damping_coefficient,obj.x_end,obj.mass);

            xds_spring(1,1:2) = yd_;
            xds_spring(1,3:4) = zd_;
            xds_spring(1,3:4) = xds_spring(1,3:4) + forcing_terms(1,:)/obj.tau; 
            ts_exp = obj.tau*obj.xs_phase;
            ts_exp_flip = flip(ts_exp);
            ts = linspace(0,1.5*obj.tau,obj.n_time_steps);
            dts_exp_flip = zeros(obj.n_time_steps,1);
            for tts=2:obj.n_time_steps
                dts(tts) = ts(tts)-ts(tts-1);
                dts = flip(dts);
                dts_exp_flip(tts) = ts_exp_flip(tts)-ts_exp_flip(tts-1);
            end
            for tt=2:obj.n_time_steps
                dt = dts_exp_flip(tt);
                xs_spring(tt,:) = xs_spring(tt-1,:) + dt*xds_spring(tt-1,:);
                [yd_,zd_] = dE_spring(xs_spring(tt,:),obj.tau,obj.spring_constant,obj.damping_coefficient,obj.x_end,obj.mass);
                xds_spring(tt,1:2) = yd_;
                xds_spring(tt,3:4) = zd_;
                xds_spring(tt,3:4) = zd_ + forcing_terms_flip(tt,:)/obj.tau; 
            end

            y_out = xs_spring(:,1:2);
            yd_out = xds_spring(:,1:2);
            ydd_out = xds_spring(:,3:4)/obj.tau;
            release_idxs = find(ts_exp_flip>=0.6);
            release_idx = release_idxs(1);
            release_point = y_out(release_idx,:);
            dt = 0.01;
            ydd_ball = ydd_out(1:release_idx,:);
            yd_ball = yd_out(1:release_idx,:);
            y_ball = y_out(1:release_idx,:);

            for i = release_idx+1:obj.n_time_steps
                ydd_ball(i,1) = 0.0;
                ydd_ball(i,2) = -9.81;
                yd_ball(i,:) = yd_ball(i-1,:) + dt*ydd_ball(i,:);
                y_ball(i,:) = y_ball(i-1,:) + dt*yd_ball(i,:);
                if y_ball(i,2) < -0.3
                    y_ball(i,2) = -0.3;
                    yd_ball(i,:) = zeros(1,2);
                    ydd_ball(i,:) = zeros(1,2);
                end
            end
            T = release_idx;
            while y_ball(T,2) > -0.3
                T = T+1;
                ydd_ball(T,:) = [0.0,-9.81];
                yd_ball(T,:) = yd_ball(T-1,:) + dt*ydd_ball(T,:);
                y_ball(T,:) = y_ball(T-1,:) + dt*yd_ball(T,:);
                if y_ball(T,2) <= -0.3
                    y_ball(T,2) = -0.3;
                    yd_ball(T,:) = zeros(1,2);
                    ydd_ball(T,:) = zeros(1,2);
                end
            end
            m = [ts_exp_flip,y_out,yd_out,ydd_out];
            if T > obj.n_time_steps
                add = repmat(m(end,:),T-obj.n_time_steps,1);
                m = [m;add];
            end
            ydd = m(:,6:7);
            y = m(:,2:3);
            if saving
                %disp("saved as: "+file_out)
                writematrix(m,file_out);
            end
        end
        
        %Plotting the trowing task
        function plot_rollout(obj,mean,col_ball,lineW_traj)
            if ~exist('col_ball','var')
                % third parameter does not exist, so default it to something
                col_ball = 'c';
            end
            if ~exist('lineW_traj','var')
                % third parameter does not exist, so default it to something
                lineW_traj = 0.3;
            
            end
            col_ball_traj = "--"+col_ball;
            x_left = -0.9;
            x_right = 0.3;
            axis([x_left x_right -0.5 0.3])
            hold on
            %plotting floor
            line([x_left,-0.71],[-0.3,-0.3],'Color','black');
            line([-0.7,-0.71],[-0.35,-0.3],'Color','black');
            line([-0.7,-0.69],[-0.35,-0.3],'Color','black');
            line([-0.69,x_right],[-0.3,-0.3],'Color','black');
            plot(-0.7,-0.35,'mo');
            
            [rows,cols]=size(mean);
            if cols == 2*obj.n_rbf
                for ii = 1:rows
                    [y,~,y_ball,r_point] = dynamics(obj,mean(ii,1:obj.n_rbf),mean(ii,obj.n_rbf+1:end),0,"");
                    plot(y(:,1),y(:,2),'LineWidth',0.3)
                    plot(y_ball(:,1),y_ball(:,2),'--c','LineWidth',0.3)
                    plot(r_point(1),r_point(2),'g*');
                end
            else
                [y,~,y_ball,r_point] = dynamics(obj,obj.mean_x,obj.mean_y,0,"");
                if col_ball ~= "c"
                    plot(y(:,1),y(:,2),col_ball,'LineWidth',lineW_traj)
                else
                    plot(y(:,1),y(:,2),'LineWidth',lineW_traj)
                end
                %plotting ball curve
                plot(y_ball(:,1),y_ball(:,2),col_ball_traj,'LineWidth',0.3) 
                plot(y_ball(1:10:end,1),y_ball(1:10:end,2),'ko','LineWidth',0.3)
                plot(r_point(1),r_point(2),'g*');
            end
        end
        
        %Make trajectory invariant to rotodilatation
        function M = rotodilatation(obj,x0,x1)
            x0_norm = x0/norm(x0);
            x1_norm = x1/norm(x1);
            M0 = fnAR(obj,x0_norm);
            M1 = fnAR(obj,x1_norm);
            M = M1 * M0' * norm(x1) / norm(x0);
        end
        
        %Compute the Rotation for the rotodilatation function
        function R = fnAR(obj,x)
            n = length(x);
            R = eye(n);
            step = 1;
            while (step<n)
                A = eye(n);
                it=1;
                while (it < n-step+1)
                    r2 = x(it)*x(it) + x(it+step) * x(it+step);
                    if (r2>0)
                        r = sqrt(r2);
                        pcos = x(it)/r;
                        psin = -x(it+step)/r;
                        A(it,it) = pcos;
                        A(it,it+step) = -psin;
                        A(it+step,it) = psin;
                        A(it+step,it+step) = pcos;
                    end
                    it = it+2*step;
                end
                step = step*2;
                x = x*A;
                R = A*R;
            end
        end
        
        %Plotting general trajectories
        function plot_trajectory(obj,mean)
            hold on
            [rows,cols]=size(mean);
            if cols == 2*obj.n_rbf
                for ii = 1:rows
                    [y,~,y_ball,r_point] = dynamics(obj,mean(ii,1:obj.n_rbf),mean(ii,obj.n_rbf+1:end),0,"");
                    plot(y(:,1),y(:,2))
                    %plotting ball curve
                    plot(y_ball(:,1),y_ball(:,2),'--c','LineWidth',0.3)
                    plot(y_ball(1:10:end,1),y_ball(1:10:end,2),'ko','LineWidth',0.3)
                    plot(r_point(1),r_point(2),'g*');
                end
            else
                [y,~,y_ball,r_point] = dynamics(obj,obj.mean_x,obj.mean_y,0,"");
                plot(y(:,1),y(:,2))
            end
        end
    end
end


