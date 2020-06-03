function [dd_theta, dd_x] = dyn_equation(m_p, m_c, g, l, theta, d_theta, d_x, F, b)

%theta = abs(theta - pi);

%dd_x=(2*m_p*l*d_theta^2*sin(theta)+3*m_p*g*sin(theta)*cos(theta)+4*F-4*b*d_x) / (4*(m_c+m_p)-3*m_p*cos(theta)^2);
%dd_theta=(-3*m_p*l*d_theta^2*sin(theta)*cos(theta)-6*(m_c+m_p)*g*sin(theta)-6*(F-b*d_x)*cos(theta)) / (4*l*(m_c+m_p)-3*m_p*l*cos(theta)^2);

dd_x=(-2*m_p*l*d_theta^2*sin(theta)+3*m_p*g*sin(theta)*cos(theta)+4*F-4*b*d_x) / (4*(m_c+m_p)-3*m_p*cos(theta)^2);
dd_theta=(-3*m_p*l*d_theta^2*sin(theta)*cos(theta)+6*(m_c+m_p)*g*sin(theta)+6*(F-b*d_x)*cos(theta)) / (4*l*(m_c+m_p)-3*m_p*l*cos(theta)^2);

end

