function data=loadoracle(path,tasknum)
    for t=1:tasknum
        x=csvread(sprintf('%s/%d/share.csv',path,t));
        u=unique(x(:,2));
        data{t}=zeros(ceil(size(x,1)/length(u)),length(u));
        for i=1:length(u)
            index=find(x(:,2)==u(i));
            data{t}(1:length(index),i)=x(index,3);
        end
    end   
end