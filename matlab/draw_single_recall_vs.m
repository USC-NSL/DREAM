clear;
hold all;
x(:,:,1)=load_metric('E:/enl/measurement/DynamicMonitor/outputserver/output00_k14/0.010_1000000_a+0-3/HHHMetrics.csv');
x(:,:,2)=load_metric('E:/enl/measurement/DynamicMonitor/outputserver/output00_k14/0.010_1000000_a+3-6/HHHMetrics.csv');
x(:,:,3)=load_metric('E:/enl/measurement/DynamicMonitor/outputserver/output00_k14/0.010_1000000_a+6-9/HHHMetrics.csv');
x(:,:,4)=load_metric('E:/enl/measurement/DynamicMonitor/outputserver/output00_k14/0.010_1000000_a+9-12/HHHMetrics.csv');
max_data=0.01*[401421638, 427186609, 429176540, 421694749];
cols=[12 14];
x(:,cols,1)=x(:,cols,1)./max_data(1);
x(:,cols,2)=x(:,cols,2)./max_data(2);
x(:,cols,3)=x(:,cols,3)./max_data(3);
x(:,cols,4)=x(:,cols,4)./max_data(4);

switchmemindex=3;
hhhweightmeanindex=12;
hhhweightmaxindex=14;

%plot recall vs column
switch_num=8;
recall_col=20;
x_col=switchmemindex;
series_col=3;
y=mean(x,3);
%indexes=and(y(:,2)==switch_num,or(y(:,1)==256,or(y(:,1)==1024,y(:,1)==4096)));
%indexes=y(:,2)==switch_num;
%draw_metric_multiple_switch(y(indexes,:),series_col,x_col,recall_col)
z=y(y(:,3)==0,:);
z(:,3)=z(:,1);%/switch_num;%copy the size of the switch
z2=z;
z(1,:)=[];
plot(z(:,x_col),z(:,recall_col),'DisplayName','Single');
%plot(z(:,x_col),z(:,recall_col),'DisplayName',sprintf('Single/%d',switch_num));
z=z2;
%plot(z(1,x_col),z(1,recall_col),'+','LineWidth',2,'MarkerSize',12,'DisplayName','Real HHH');
legend off
legend show

xlabel('A switch capacity')
%xlabel('Average HHH Weight')
%xlabel('Max HHH Weight')
ylabel('Recall');
title(sprintf('Series on Resource for %d switches',switch_num));
