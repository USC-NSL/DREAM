function draw_droprej(drop,rej,xl,xvalues,leg,marks,rejfor,dropfor)
    if (nargin<5)
        leg={'DREAM-reject';'Fixed-reject';'DREAM-drop'};
        marks={'-sk','-vr','-+k','-og','-^c','-sg','-oy','-+g'};
        rejfor=[1,3];
        dropfor=[1];
    end
    drop=drop*100;
    rej=rej*100;
    
    figure; 
    hold all;
    for i=1:length(rejfor)
            plot(xvalues,rej(:,rejfor(i)),marks{i},'LineWidth',2,'MarkerSize',10);
    end
    for i=1:length(dropfor)
            plot(xvalues,drop(:,dropfor(i)),marks{i+length(rejfor)},'LineWidth',2,'MarkerSize',10);
    end
    

    %plot(xvalues,rej(:,3),marks{2},'LineWidth',2,'MarkerSize',10);
    
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