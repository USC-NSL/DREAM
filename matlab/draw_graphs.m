function draw_graphs(f1,f2,f3,f4,xvalues,xl)
%marks={'-ob','-+k','-sr','-^g','-^c','-sm','-oy','-+g'};
marks={'-vk','-vb','-+r','-^g','-^c','-sg','-oy','-+g'};
ylabels={'Reject Tasks','Drop Tasks','Drop Age','Satisfaction','Utilization'};
leg={'DREAM_old';'DREAM';'Equal';'Fixed_8';'Fixed_4';'Fixed_32';'Fixed_16'};
index=[2,3,6];

%leg={'DREAM';'Equal';'Fixed_32'};
%index=[1,2,3];


for c=1:4;
    figure; hold all; 
    for i2=1:min(length(index),size(f1,1)); 
        i=index(i2);
        plot(xvalues,[f1(i,c) f2(i,c) f3(i,c) f4(i,c)],marks{i},'LineWidth',2); 
    end;
    l=legend(leg{index});
    set(l,'interpreter','none');
    set(l,'Location','Best');
    ylabel(ylabels{c})
    xlabel(xl);
    ylim([0,1]);
    set(gca,'XTick',xvalues);
    set(findall(gcf,'type','text'),'fontSize',14')
    set(findobj('type','axes'),'fontsize',14)

    set(l,'fontsize',12);
    xlim([min(xvalues)*0.95,max(xvalues)*1.05]);
end
end