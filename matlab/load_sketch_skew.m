x={};
indexes=[6:2:14];
cols=[12 14];
max_data=0.01*[401406680, 427239098, 428963710, 417816104];
base=1;
for i=indexes
    path=sprintf('E:/enl/measurement/DynamicMonitor/outputsketch/output00_k_%02.f',i);
    if (i==10)
       path='E:/enl/measurement/DynamicMonitor/outputsketch/output00';
    end
    x{length(x)+1}=load_metric(sprintf('%s/0.010_1000000_a+0-3/HHHMetrics.csv',path));
    x{length(x)+1}=load_metric(sprintf('%s/0.010_1000000_a+3-6/HHHMetrics.csv',path));
    x{length(x)+1}=load_metric(sprintf('%s/0.010_1000000_a+6-9/HHHMetrics.csv',path));
    x{length(x)+1}=load_metric(sprintf('%s/0.010_1000000_a+9-12/HHHMetrics.csv',path));
    x{base}(:,cols)=x{base}(:,cols)./max_data(1);
    x{base+1}(:,cols)=x{base+1}(:,cols)./max_data(2);
    x{base+2}(:,cols)=x{base+2}(:,cols)./max_data(3);
    x{base+3}(:,cols)=x{base+3}(:,cols)./max_data(4);
    base=base+4;
end