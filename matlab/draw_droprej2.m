function draw_droprej2(drop,rej,xl,xvalues,leg,marks)
    drop=100*drop;
    rej = 100* rej;
    if (nargin<6)
        marks={'-sk','-vb','-+k','-og','-^c','-sg','-oy','-+g'};
        if (nargin<5)
            leg={'DREAM-reject';'Fixed-reject';'DREAM-drop'};        
        end
    end
    figure; 
    hold all;
    plot(xvalues,rej(:,1),marks{1},'LineWidth',2,'MarkerSize',10);
    plot(xvalues,rej(:,2),marks{2},'LineWidth',2,'MarkerSize',10);
    plot(xvalues,rej(:,5),marks{3},'LineWidth',2,'MarkerSize',10);
    plot(xvalues,rej(:,6),marks{4},'LineWidth',2,'MarkerSize',10);
    plot(xvalues,drop(:,1),marks{5},'LineWidth',2,'MarkerSize',10);
    plot(xvalues,drop(:,2),marks{6},'LineWidth',2,'MarkerSize',10);
    l=legend(leg);
    ylabel('% of tasks');
    xlabel(xl);
    set(l,'interpreter','none');
    set(findall(gcf,'type','text'),'fontSize',13)
    set(findobj(gcf,'type','axes'),'fontsize',13)

    set(l,'fontsize',10);
    xlim([min(xvalues)*0.95,max(xvalues)*1.05]);
    ylim([0,100]);
    set(gca,'XScale','log');
    set(gca,'Xtick',xvalues);
    set(gcf,'OuterPosition',[500,500,375,360])
    
end