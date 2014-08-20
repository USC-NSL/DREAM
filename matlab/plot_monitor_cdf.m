function plot_monitor_cdf(x,base)
    %base=7; 
    figure; 
    hold all; 
    cc=jet(size(x,1));
    for i=1:size(x,1), 
        plot(0:32,x(i,base+[0:32]*2+1),'DisplayName',sprintf('%d,%d,%d',x(i,1),x(i,2),x(i,3)),'color',cc(i,:)); 
    end
    legend show;
end