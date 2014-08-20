col=4;
path='E:/enl/measurement/DynamicMonitor/outputscenario/precision2/k_06_0.8_u_5/0.001_1000000_a+0-3/EqualAvgThreshold_1000_2';
t=load_multitask(path);
x(1,:,1)= [mean(t(:,col,1)+t(:,col,2))/2, mean(abs(t(:,col,1)-t(:,col,2)))];
path='E:/enl/measurement/DynamicMonitor/outputscenario/precision2/k_06_0.8_u_5/0.001_1000000_a+0-3/EqualAvgThreshold_1500_2';
t=load_multitask(path);
x(2,:,1)= [mean(t(:,col,1)+t(:,col,2))/2, mean(abs(t(:,col,1)-t(:,col,2)))];
path='E:/enl/measurement/DynamicMonitor/outputscenario/precision2/k_06_0.8_u_5/0.001_1000000_a+0-3/EqualAvgThreshold_2000_2';
t=load_multitask(path);
x(3,:,1)= [mean(t(:,col,1)+t(:,col,2))/2, mean(abs(t(:,col,1)-t(:,col,2)))];

path='E:/enl/measurement/DynamicMonitor/outputscenario/avg_hhh/k_06_0.8_u_5/0.001_1000000_a+0-3/EqualAvgThreshold_1000_2';
t=load_multitask(path);
x(1,:,2)= [mean(t(:,col,1)+t(:,col,2))/2, mean(abs(t(:,col,1)-t(:,col,2)))];
path='E:/enl/measurement/DynamicMonitor/outputscenario/avg_hhh/k_06_0.8_u_5/0.001_1000000_a+0-3/EqualAvgThreshold_1500_2';
t=load_multitask(path);
x(2,:,2)= [mean(t(:,col,1)+t(:,col,2))/2, mean(abs(t(:,col,1)-t(:,col,2)))];
path='E:/enl/measurement/DynamicMonitor/outputscenario/avg_hhh/k_06_0.8_u_5/0.001_1000000_a+0-3/EqualAvgThreshold_2000_2';
t=load_multitask(path);
x(3,:,2)= [mean(t(:,col,1)+t(:,col,2))/2, mean(abs(t(:,col,1)-t(:,col,2)))];

path='E:/enl/measurement/DynamicMonitor/outputscenario/fixed/k_06_0.8_u_5/0.001_1000000_a+0-3/Fixed_1000_2';
t=load_multitask(path);
x(1,:,3)= [mean(t(:,col,1)+t(:,col,2))/2, mean(abs(t(:,col,1)-t(:,col,2)))];
path='E:/enl/measurement/DynamicMonitor/outputscenario/fixed/k_06_0.8_u_5/0.001_1000000_a+0-3/Fixed_1500_2';
t=load_multitask(path);
x(2,:,3)= [mean(t(:,col,1)+t(:,col,2))/2, mean(abs(t(:,col,1)-t(:,col,2)))];
path='E:/enl/measurement/DynamicMonitor/outputscenario/fixed/k_06_0.8_u_5/0.001_1000000_a+0-3/Fixed_2000_2';
t=load_multitask(path);
x(3,:,3)= [mean(t(:,col,1)+t(:,col,2))/2, mean(abs(t(:,col,1)-t(:,col,2)))];
% res=[1000,1500,2000]; plot(res,x(:,1,1))
% hold all;
% plot(res,x(:,1,2))
% plot(res,x(:,1,3))