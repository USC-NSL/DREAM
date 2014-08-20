clear;
%figure;
%indexes=[0:2:8 10 20];
indexes=[4:2:16];
colors=jet(length(indexes));
hold all;
k=1;
for i=indexes
    clear x y z;
    %path=sprintf('E:/enl/measurement/DynamicMonitor/outputserver/output00_rm_%03.f_5',i);
    path=sprintf('E:/enl/measurement/DynamicMonitor/outputserver/output00_k%02.f',i);
    if i==10
        path='E:/enl/measurement/DynamicMonitor/outputserver/output00';
    end
    
    x(:,:,1)=load_metric(sprintf('%s/0.010_1000000_a+0-3/HHHMetrics.csv',path));
    x(:,:,2)=load_metric(sprintf('%s/0.010_1000000_a+3-6/HHHMetrics.csv',path));
    x(:,:,3)=load_metric(sprintf('%s/0.010_1000000_a+6-9/HHHMetrics.csv',path));
    x(:,:,4)=load_metric(sprintf('%s/0.010_1000000_a+9-12/HHHMetrics.csv',path));
    
    %max_data=0.01*[1.009e+015, 1.4542e+015,  1.2119e+015,  1.2651e+015];
    max_data=0.01*[401421638, 427186609, 429176540, 421694749];
    %max_data=0.01*[1.4736e+006, 1.5103e+006, 1.4352e+006, 1.5775e+006];
    %max_data=0.01*[1.4688e+005, 1.4281e+005, 1.5795e+005, 1.4944e+005];
    cols=[12 14];
    x(:,cols,1)=x(:,cols,1)./max_data(1);
    x(:,cols,2)=x(:,cols,2)./max_data(2);
    x(:,cols,3)=x(:,cols,3)./max_data(3);
    x(:,cols,4)=x(:,cols,4)./max_data(4);

    switchmemindex=3;
    hhhweightmeanindex=12;
    hhhweightmaxindex=14;

    %plot recall vs column
    switch_num=4;
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
    plot(z(:,x_col),z(:,recall_col),'DisplayName',sprintf('%.2f',i/10),'color',colors(k,:));
    %plot(z(:,x_col),z(:,recall_col),'DisplayName',sprintf('Single/%d',switch_num));
    z=z2;
    %plot(z(1,x_col),z(1,recall_col),'+','LineWidth',2,'MarkerSize',12,'DisplayName','Real HHH');
    k=k+1;
end
legend off
legend show

xlabel('A switch capacity')
%xlabel('Average HHH Weight')
%xlabel('Max HHH Weight')
ylabel('Recall');
title(sprintf('Series on Resource for %d switches',switch_num));