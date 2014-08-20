function draw_droprej_fixed(rej,xl,xvalues,leg,marks)
    if (nargin<5)
        marks={'-sk','-vb','-+r','-og','-^c','-sg','-oy','-+g'};
        if (nargin<4)
            leg={'DREAM-reject';'Fixed-reject';'DREAM-drop'};
        end
    end
    
    figure; 
    hold all;
    for i=1:size(rej,2)
        plot(xvalues,rej(:,i),marks{i},'LineWidth',2);
    end    
    l=legend(leg);
    ylabel('% of tasks');
    xlabel(xl);
    set(l,'interpreter','none');
    set(findall(gcf,'type','text'),'fontSize',14)
    set(findobj(gcf,'type','axes'),'fontsize',14)

    set(l,'fontsize',9);
    xlim([min(xvalues)*0.95,max(xvalues)*1.05]);
    ylim([0,1]);
    set(gca,'XScale','log');
    set(gca,'Xtick',xvalues);
    set(gcf,'OuterPosition',[500,500,350,360])
    
end