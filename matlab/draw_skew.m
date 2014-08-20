x=csvread('../output/skew/2/skew1_w_16.txt');
%figure; 
hold all; 
for i=1:max(x(:,1))+1, 
    t=i-1;plot(x(x(:,1)==t,3)/sum(x(x(:,1)==t,3))); 
end,  
set(gca,'XScale','log');  
set(gca,'YScale','log'); 
xlabel('ith largest traffic');
ylabel('Traffic fraction');