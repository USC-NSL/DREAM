function y=extractametric(x,c1,c2)
 y=zeros(size(x,2),0); 
 x1=x(:,:,c1)';
 x2=x(:,:,c2)';
 for i=1:size(x1,2), 
     y=[y,x1(:,i), x2(:,i)]; 
 end
end