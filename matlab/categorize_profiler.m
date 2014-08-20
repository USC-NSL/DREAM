function out=categorize_profiler(data1,data2,data3,keywords)
    %profile_length=length(data1.data)./length(unique(data1.textdata));   
    j=1;
    for i=1:length(keywords)
        k=keywords{i};
        index=strcmp(data1.textdata(:,2),k);
        if sum(index)>0
            out(j,:)=data1.data(index);
            j=j+1;
        else
            index=strcmp(data2.textdata(:,2),k);
            if sum(index)>0
                out(j,:)=data2.data(index);
                j=j+1;
            else
                index=strcmp(data3.textdata(:,2),k);
                if sum(index)>0
                    out(j,:)=data3.data(index);
                    j=j+1;
                end
            end
        end        
    end
end