function stats=load_hhh_missed(path)
    th=1000000;
    files={'dream_h5f','equal','fixed_32'};
    for f=1:length(files)
        k=1;
        m=[];
        display(sprintf('Loading %s',files{f}));
        for i=1:4096,             
            x=csvread(sprintf('%s/%s/%d/HHHMisedMetrics.csv',path,files{f},i),1,0); 
            index=(x(:,2)>0); 
            if sum(index)>0, 
                m(k,1)=mean((x(index,3)-th)./(x(index,2)-th)); 
                m(k,2)=mean((x(index,5)-th)./(x(index,4)-th)); 
                m(k,3)=mean((x(index,3))./(x(index,2))); 
                m(k,4)=mean((x(index,5))./(x(index,4))); 
                k=k+1; 
            end, 
        end
        if isempty(m)
            stats(f,:)=[0,0,0,0];
        else
            stats(f,:)=mean(m,1);
        end
        
    end
end