function recall_hhh_single2(path,s,mark)
     r=dir(path);
     filenames={};
     k=1;
    for i=1:length(r)
        if r(i).isdir
            if ~isempty(regexp(r(i).name,'SingleSwitch','once'))
                filenames{k}=r(i).name;
                k=k+1;
            end
        end    
    end
    colors=jet(k-1);
    capacity=zeros(k-1,1);
    for i=1:k-1
        l=regexp(filenames{i}, '_', 'split'); 
        l=str2double(l{2}); 
        recall_col=10;
        r=csvread(sprintf('%s/%s/HHHMetrics.csv',path,filenames{i}),1,0);
        r=r(:,9);
        x=csvread(sprintf('%s/%s/HHHMonitorMetrics.csv',path,filenames{i}),1,0);
        x=x(:,11)./x(:,4);
        out(:,:,i)=[x(1:length(r)),r];
        capacity(i)=l;
    end
    [capacity,capacityi]=sort(capacity);
    for i=1:length(capacity)
        ci=capacityi(i);
        plot(out(:,1,ci),out(:,2,ci),mark,'color',colors(i,:),...
            'DisplayName',sprintf('%s,%d',s,capacity(i)));
    end    
    xlabel('Predicted accuracy');
    ylabel('Precision');
    legend show;
end