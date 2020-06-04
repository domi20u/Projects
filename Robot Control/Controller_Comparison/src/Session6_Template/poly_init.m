function [a]=poly_init(t_init,t_final)


a(1)=-(10*t_final^2*t_init^3 - 5*t_final*t_init^4 + t_init^5)/((t_final - t_init)^2*(3*t_final*t_init^2 - 3*t_final^2*t_init + t_final^3 - t_init^3));
a(2)=(30*t_final^2*t_init^2)/((t_final - t_init)^2*(3*t_final*t_init^2 - 3*t_final^2*t_init + t_final^3 - t_init^3));
a(3)=-(30*t_init*(t_final*t_init + t_final^2))/((t_final - t_init)^2*(3*t_final*t_init^2 - 3*t_final^2*t_init + t_final^3 - t_init^3));
a(4)=(10*(4*t_final*t_init + t_final^2 + t_init^2))/((t_final^2 - 2*t_final*t_init + t_init^2)*(3*t_final*t_init^2 - 3*t_final^2*t_init + t_final^3 - t_init^3));
a(5)=-(15*(t_final + t_init))/((t_final - t_init)*(6*t_final^2*t_init^2 - 4*t_final*t_init^3 - 4*t_final^3*t_init + t_final^4 + t_init^4));
a(6)=6/((t_final^2 - 2*t_final*t_init + t_init^2)*(3*t_final*t_init^2 - 3*t_final^2*t_init + t_final^3 - t_init^3));
