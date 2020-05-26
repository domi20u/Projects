function [xs,xds] = exponential_fcn(alpha,tau,n_time_steps,initial_state, attractor_state)
    %disp(size(ts))
    ts = linspace(0,1.5*tau,n_time_steps)';
    exp_term = exp(-alpha*ts/tau);
    %x = [exp_term,exp_term];
    xd = -alpha/tau * exp_term;
    
    val_range = initial_state - attractor_state;
    %disp(val_range)
    %disp(attractor_state)
    xs = val_range .* exp_term + attractor_state;
    %figure
    %plot(ts,exp_term)
    xds = val_range .* xd;
    %figure
    %plot(ts,xs)
end

