function output=load_metric(filename)
%'E:/enl/measurement/DynamicMonitor/output2/0.010_1000000_a+3-6/MonitorMetrics.csv'
    data=importdata(filename,',',1);
    labels=data.textdata(:,1);
    for i=1:length(labels), 
        l{i}=regexp(labels{i}, '_', 'split'); 
    end
    l(1)=[];
    for i=1:length(l), 
        li=l{i}; 
        for j=1:length(li)-1, 
            param(i,j)=str2double(li{j+1}); 
        end, 
    end
    output = [param,data.data];
    %fix single less columns
    if all(output(:,2)==1)
        y(:,[1 2])=output(:,[1 2]);
        y(:,3)=zeros(size(output,1),1);
        y(:,[4:size(output,2)+1])=output(:,[3:size(output,2)]);
        output=y;
    end
end