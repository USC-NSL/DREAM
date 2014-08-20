clear;
figure;
%indexes=[0:2:8];
%indexes=[0:2:8 10 20];
indexes=[6:2:14];
colors=jet(length(indexes));
hold all;
k=1;
for i=indexes
    clear x y z;
    %path=sprintf('E:/enl/measurement/DynamicMonitor/outputsketch2/output00_ra_%02.f',i);
    path=sprintf('E:/enl/measurement/DynamicMonitor/outputsketch2/00_k%02.f',i);    
    if (i==10)
       path='E:/enl/measurement/DynamicMonitor/outputsketch2/00';
    end
    
    x(:,:,1)=load_metric(sprintf('%s/0.001_1000000_a+0-3/HHHMetrics.csv',path));
    %x(:,:,2)=load_metric(sprintf('%s/0.010_1000000_a+3-6/HHHMetrics.csv',path));
    %x(:,:,3)=load_metric(sprintf('%s/0.010_1000000_a+6-9/HHHMetrics.csv',path));
    %x(:,:,4)=load_metric(sprintf('%s/0.010_1000000_a+9-12/HHHMetrics.csv',path));
    
    max_data=0.01*[401406680, 427239098, 428963710, 417816104];
    cols=[12 14];
    x(:,cols,1)=x(:,cols,1)./max_data(1);
   %x(:,cols,2)=x(:,cols,2)./max_data(2);
   %x(:,cols,3)=x(:,cols,3)./max_data(3);
   %x(:,cols,4)=x(:,cols,4)./max_data(4);

    hhhweightmeanindex=12;
    hhhweightmaxindex=14;

    %plot recall vs column
    switch_num=4;
    recall_col=20;
    series_col=3;    
    y=mean(x,3);
    z=y(y(:,2)~=1,:);
    resource_usage=calculate_sketch_memory(z(:,1),z(:,3),z(:,2));
    [resource_usage,resource_usage_i]=sort(resource_usage);
    plot(resource_usage,z(resource_usage_i,recall_col),'DisplayName',sprintf('%.2f',i/10),'color',colors(k,:));
    k=k+1;
end
legend off
legend show

xlabel('A switch capacity')
%xlabel('Average HHH Weight')
%xlabel('Max HHH Weight')
ylabel('Recall');
title(sprintf('Series on Resource for %d switches',switch_num));