function y=myturn(x)
%used to create input for drawmetric 1 2; 3 4 will be 1 2 3 4, 
% 2 and 4 are errors
y=x(:,[1,2]);
y=y';
y=y(:);
end