function draw_graphs_for_headroom(f1,f2,f3,f4,f5,xvalues,xl)
marks={'-ob','-+k','-sr','-^g','-^c','-sm','-oy','-+g'};
ylabels={'Reject Tasks','Drop Tasks','Drop Age','Satisfaction','Utilization'};
leg={'No Headroom';'Headroom=1%';'Headroom=10%';'Headroom=5%';'5-Drop'};
index=[3,4,2,1];
for c=1:size(f1,2);
    figure; hold all; 
    for i2=1:min(length(index),size(f1,1)); 
        i=index(i2);
        plot(xvalues,[f1(i,c) f2(i,c) f3(i,c) f4(i,c) f5(i,c)],marks{i},'LineWidth',2); 
    end;
    l=legend(leg{index});
    set(l,'interpreter','none');
    set(l,'Location','Best');
    ylabel(ylabels{c})
    set(findall(gcf,'type','text'),'fontSize',14')
    set(findobj('type','axes'),'fontsize',14)
    xlabel(xl);
    ylim([0,1]);
end
end