function [params,centers,widths] = rbfn_fit(n_basis_functions,targets,bf_type)

intersection_height = 0.7;
n_time_steps = length(targets);
weights = eye(n_time_steps);
xt = linspace(0,1,n_time_steps);
centers = linspace(0,1,n_basis_functions);
widths = ones(n_basis_functions,1);
c = zeros(1,n_basis_functions);
for i =1:n_basis_functions
    c(i) = exp(-2.145*i*1.43/n_basis_functions);
end
if bf_type == "mullifier"
    %for basis function calculation from dmp++
    %centers = c;
    for i=2:n_basis_functions
        w = 1/(centers(i)-centers(i-1));
        widths(i) = w;
    end
    widths(1) = widths(2);
else
    for i=1:n_basis_functions-1
        w = sqrt((centers(i+1)-centers(i))^2/(-8*log(intersection_height)));
        widths(i) = w;
    end
    widths(n_basis_functions) = widths(n_basis_functions-1);
end

ac = zeros(n_time_steps,n_basis_functions);
for i=1:n_basis_functions 
    if bf_type == "mullifier"
        ac(:,i) = mullifier(xt,centers(i),widths(i));
    else
        ac(:,i) = rbf(xt,centers(i),widths(i));
    end
end

params = (ac'*weights*ac)\(ac'*weights*targets);
centers = centers';
%writematrix([params,centers',widths],name);

end

function activation = rbf(x,c,w)
    activation = exp(-0.5*1/(w)^2 * (x-c).^2);
end
function activation = mullifier(x,c,w)
    term = abs(w*(x-c));
    activation = exp(-1./(1-term.^2)) .* (term<1);
    activation(isnan(activation)) = 0;
end