%% Predict trajectories from the trained basis function networks

function [out,rbfn] = rbfn_predict(in,params,centers,widths,bf_type)
n = length(params);
n_x = length(in);
rbfn = zeros(n,n_x);
out = zeros(1,n_x);
for i = 1:n   
    if bf_type == "mollifier"
        rbfn(i,:) = params(i)*mollifier(in,centers(i),widths(i));
    else
        rbfn(i,:) = params(i)*rbf(in,centers(i),widths(i));
    end
    out = out + rbfn(i,:);
end

end

function activation = rbf(x,c,w)
    activation = exp(-0.5*1/(w)^2 * (x-c).^2);
end

%basis function according to dmp++
function activation = mollifier(x,c,w)
    term = abs(w*(x-c));
    
    activation = exp(-1./(1-term.^2)) .* (term<1);
    activation(isnan(activation)) = 0;
end