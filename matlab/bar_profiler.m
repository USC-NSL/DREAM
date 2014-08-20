function [m,s,l,h,tags]=bar_profiler(path,file)
    r=dir(path);
    filenames={};
    k=1;
    for i=1:length(r)
        if r(i).isdir
            if isempty(regexp(r(i).name,'^\.+$','once'))            
                filenames{k}=r(i).name;
                k=k+1;
            end
        end    
    end
    for f=1:k-1
        display(sprintf('Loading %s',filenames{f}));
        data=importdata(sprintf('%s/%s/dream_h5/%s',path,filenames{f},file));
        t=str2double(data.textdata(:,1));
        index=and(t>=307,t<2475);
        data.data=data.data(index);
        data.textdata=data.textdata(index,:);
        maxX=0; 
        tags=unique(data.textdata(:,2));
        for i=1:length(tags); 
            [m(f,i),s(f,i),l(f,i),h(f,i)]=draw_profiler_stat(data,tags{i});            
        end; 
    end
    try
        x=str2double(filenames);
        [x,index]=sort(x);        
        m=m(index,:);
        s=s(index,:);
        l=l(index,:);
        h=h(index,:);
        filenames=arrayfun(@num2str, x, 'unif', 0);
    catch e
    end
    m=m'/1e6;
    s=s'/1e6;
    l=l'/1e6;
    h=h'/1e6;
    err(:,:,1)=m-l;
    err(:,:,2)=h-m;
    figure;
    barwitherr(err,m);
    l=legend(filenames);
    set(l,'interpreter','none');
    set(gca,'XTickLabel',tags)
    set(findall(gcf,'type','text'),'fontSize',14')
    set(findobj('type','axes'),'fontsize',14)
    ylabel('Time (ms)');
    axis([0.5,size(m,1)+0.5,0,max(max(h))*1.1]);
end