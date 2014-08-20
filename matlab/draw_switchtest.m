titles={'Save','Fetch','Delete'};
for i=1:3
    figure;
    errorbar(xvalues(1:size(msw,1)),msw(:,i),ssw(:,i),'DisplayName','OVS','LineWidth',2);
    hold all;
    errorbar(xvalues(1:size(mhw,1)),mhw(:,i),shw(:,i),'DisplayName','HW','LineWidth',2);
    set(gca,'YScale','log')
    set(gca,'XScale','log')
    legend show;
    title(titles{i});
    set(findobj('type','axes'),'fontsize',14)
    set(findall(gcf,'type','text'),'fontSize',14')
    axis([12,2548,0.5,2e4]);
    ylabel('Time (ms)');
    xlabel('# Rules');
    set(gca,'XTick',xvalues)
    set(gca,'YTick',10.^[0,1,2,3,4])
end