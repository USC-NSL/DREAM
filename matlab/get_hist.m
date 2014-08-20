function [h,stats]=get_hist(folder,hx)
    stats=load_multi_task(folder);   
    h=zeros(1,length(hx)-1);
    for j=1:length(stats)
        s=stats{j};
        h2=zeros(1,length(hx)-1);
        for i=1:length(hx)-1
            h2(i)=sum(and(s(:,2)>=hx(i),s(:,2)<hx(i+1)));
        end
        h2=h2/size(s,1);
        h=h+h2;
    end
    h=h/length(stats);
end