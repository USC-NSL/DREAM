%%%%%%%%%%%%%%%%
%DON'T USE THIS AS RECALL COLUMNS DOES NOT SHOW THIS 
%BECAUSE THEY IGNORE NULL VALUES WHEN COMPUTING MEAN

figure; 
hold all;
z=[];
k=1;
indexes=[0:2:8 10 20];
for i=indexes
    if i==0
        x=load_metric('E:/enl/measurement/DynamicMonitor/outputserver/output00/0.010_1000000_a+0-3/HHHMetrics.csv');
    else
        x=load_metric(sprintf('E:/enl/measurement/DynamicMonitor/outputserver/output00_rm_%03.f_5/0.010_1000000_a+0-3/HHHMetrics.csv',i));
    end
    
    x=x(1,:);
    recall_start_index=24;
    h=zeros(1,33);
    for j=1:33;
        h(j)=x(recall_start_index+(j-1)*2);    
    end
    plot([0:32],h,'DisplayName',sprintf('%.1f',i/10));
    z(k)=h*[0:32]'./sum(h);
    display(sprintf('%d,%f',i,z(k)));
    k=k+1;
end
legend show

figure;
plot(indexes/100,z);