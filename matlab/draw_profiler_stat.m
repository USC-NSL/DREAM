function [m,s,l,h]=draw_profiler_stat(data,tag)
    indexes=cellfun((@(x) strcmp(x,tag)==1 ),data.textdata(:,2));
    x=data.data(indexes,1);
    m=mean(x);
    s=std(x);
    l=prctile(x,5);
    h=prctile(x,95);
