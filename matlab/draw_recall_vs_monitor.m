clear;
x(:,:,1)=load_metric('E:/enl/measurement/DynamicMonitor/outputserver/output00_k025/0.010_1000000_a+0-3/HHHMetrics.csv');
x(:,:,2)=load_metric('E:/enl/measurement/DynamicMonitor/outputserver/output00_k025/0.010_1000000_a+3-6/HHHMetrics.csv');
x(:,:,3)=load_metric('E:/enl/measurement/DynamicMonitor/outputserver/output00_k025/0.010_1000000_a+6-9/HHHMetrics.csv');
x(:,:,4)=load_metric('E:/enl/measurement/DynamicMonitor/outputserver/output00_k025/0.010_1000000_a+9-12/HHHMetrics.csv');

x2(:,:,1)=load_metric('E:/enl/measurement/DynamicMonitor/outputserver/output00_k025/0.010_1000000_a+0-3/MonitorMetrics.csv');
x2(:,:,2)=load_metric('E:/enl/measurement/DynamicMonitor/outputserver/output00_k025/0.010_1000000_a+3-6/MonitorMetrics.csv');
x2(:,:,3)=load_metric('E:/enl/measurement/DynamicMonitor/outputserver/output00_k025/0.010_1000000_a+6-9/MonitorMetrics.csv');
x2(:,:,4)=load_metric('E:/enl/measurement/DynamicMonitor/outputserver/output00_k025/0.010_1000000_a+9-12/MonitorMetrics.csv');

%max_data=0.01*[401421638, 427186609, 429176540, 421694749];
%max_data=0.01*[1.4736e+006, 1.5103e+006, 1.4352e+006, 1.5775e+006];
max_data=0.01*[1.4688e+005, 1.4281e+005, 1.5795e+005, 1.4944e+005];
cols=[4 6];
x2(:,cols,1)=x2(:,cols,1)/max_data(1);
x2(:,cols,2)=x2(:,cols,2)/max_data(2);
x2(:,cols,3)=x2(:,cols,3)/max_data(3);
x2(:,cols,4)=x2(:,cols,4)/max_data(4);

%plot recall vs column
monitorsnumindex=12;
monitorsewightmeanindex=4;
monitorsewightmaxindex=6;
switch_num=8;
recall_col=20;
x_col=size(x,2)+monitorsewightmeanindex;
series_col=3;
y=mean([x,x2],3);
%indexes=and(y(:,2)==switch_num,or(y(:,1)==256,or(y(:,1)==1024,y(:,1)==4096)));
indexes=y(:,2)==switch_num;
draw_metric_multiple_switch(y(indexes,:),series_col,x_col,recall_col)
z=y(y(:,3)==0,:);
z(:,3)=z(:,1)/switch_num;%copy the size of the switch
z2=z;
z(1,:)=[];
plot(z(:,x_col),z(:,recall_col),'DisplayName','Single');
%plot(z(:,x_col),z(:,recall_col),'DisplayName',sprintf('Single/%d',switch_num));
z=z2;
plot(z(1,x_col),z(1,recall_col),'+','MarkerSize',11,'DisplayName','Real HHH');
legend off
legend show

%xlabel('Number of Monitors')
xlabel('Mean Monitor Weight')
ylabel('Recall');
title(sprintf('Series on Resources for %d switches',switch_num));