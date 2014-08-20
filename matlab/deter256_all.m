function [sat,rej,drop]=load_estimate(path);
    %index=33:(256-32);
    index=69:191;

    %index=242:3837;
    filenames={'dream_h5f';'equal';'fixed_32'};
    maxlifetime=299;

    sat=zeros(length(filenames),1);
    rej=sat;
    drop=sat;
    dropage=sat;
    for f=1:length(filenames)
        path=sprintf('%s/%s',path1,filenames{f});
        v=[]; 
        lifetime=[];
        for i=index, 
            try, 
                x=csvread(sprintf('%s/%d/acc.csv',path,i)); 
                if (length(x)==maxlifetime)
                    %x[0,:]=[];%first slot does not have estimation anyway
                    v=[sum(x(:,2)>=0.8)/size(x,1);v]; 
                    lifetime=[lifetime;size(x,1)]; 
                else
                    drop(f)=drop(f)+1;
                end
            catch e;
            end; 
        end ;
        sat(f)=[prctile(v,5),prctile(v,95),mean(v)];
        rej(f)=(length(index)-length(v)-drop(f))/length(index);
        drop(f)=drop(f)/length(index);
        %drop(f)=sum(lifetime<maxlifetime)/length(index);
        dropage(f)=mean(lifetime(lifetime<maxlifetime))/maxlifetime;    
    end
end