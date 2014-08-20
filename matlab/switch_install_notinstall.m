clear;
index=[307,1165];
clear m; 
for i=1:8,  
    x=csvread(sprintf('E:/enl/measurement/DynamicMonitor/output/deter256/combinesize/simlevel4arrival64spt/realcombine/2048/dream_h5f/switch%d.txt',i));z=and(x(:,1)>=index(1),x(:,1)<=index(2)); 
    m(i,:)= mean(x(z,3)./(x(z,4)+x(z,3))); 
end