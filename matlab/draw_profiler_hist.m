function [hx,h]=draw_profiler_hist(data,tag)
    indexes=cellfun((@(x) strcmp(x,tag)==1 ),data.textdata(:,2));
    x=data.data(indexes,1);
    [h,hx]=hist(x,500);
    h=cumsum(h)/sum(h);
