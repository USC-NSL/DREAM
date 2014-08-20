function stats=load_multi_task(path)
    r=dir(path);
    filenames={};
    k=1;
    column=3;
    dropcol=2;
    for i=1:length(r)
        if r(i).isdir
            if isempty(regexp(r(i).name,'^\.+$','once'))
                filenames{k}=r(i).name;
                k=k+1;
            end
        end    
    end
    stats=cell(1,k-1);
    for i=1:k-1
       % display(sprintf('%s',filenames{i}));
        file =sprintf('%s/%s/HHHMetrics.csv',path,filenames{i});
        try
            stat=csvread(file,1,0);

            if (size(stat,2)>10)%complete metrics
                column=9;
                dropcol=4;
            end
            stats{i}=[stat(:,1),stat(:,column)];
            if (sum(stat(:,dropcol))==size(stat,1))
                stats{i}(stat(:,dropcol)==1,2)=-2;
            else
                stats{i}(stat(:,dropcol)==1,2)=-1;
            end
            
        catch err
            display(file);
            display(err);
            break;
        end
    end
end

