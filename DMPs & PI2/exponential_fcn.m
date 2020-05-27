%% Create the canonical system / clock signal

function [xs,xds] = exponential_fcn(alpha,tau,n_time_steps,initial_state, attractor_state)
    ts = linspace(0,1.5*tau,n_time_steps)';
    exp_term = exp(-alpha*ts/tau);
    xd = -alpha/tau * exp_term;
    val_range = initial_state - attractor_state;
    xs = val_range .* exp_term + attractor_state;
    xds = val_range .* xd;
end

