function recall_hhh_single_sketch(path,func1,s,mark)
     r=dir(path);
     filenames={};
     k=1;
    for i=1:length(r)
        if r(i).isdir
            if ~isempty(regexp(r(i).name,'CountMinSketch','once'))
                filenames{k}=r(i).name;
                k=k+1;
            end
        end    
    end
    colors=jet(k-1);
    capacity=zeros(k-1,1);
    for i=1:k-1
        l=regexp(filenames{i}, '_', 'split'); 
        l=calculate_sketch_memory(str2double(l{2}),str2double(l{4}),str2double(l{3})); 
        [r,h]=load_recall_hhh_timeseries(sprintf('%s/%s',path,filenames{i}));
        x=cellfun(func1,h, 'UniformOutput', false);
        x=cellfun((@(x) x(:,1)),x);        
        out(:,:,i)=[x(1:length(r)),r];
        capacity(i)=l;
    end
    [capacity,capacityi]=sort(capacity);
    for i=1:length(capacity)
        ci=capacityi(i);
        plot(out(:,1,ci),out(:,2,ci),mark,'color',colors(i,:),...
            'DisplayName',sprintf('%s,%d',s,capacity(i)));
    end
    legend show
end