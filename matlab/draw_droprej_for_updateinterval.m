function draw_droprej_for_updateinterval(drop,rej,xl,xvalues,leg)
    drop = drop *100;
    rej=rej * 100;
    marks={'s','v','+','o'};
    colors={'black','blue','red','green'};
    figure; 
    hold all;
    leg1={};
    leg2={};
    for i=1:4
        r(i)=plot(xvalues,rej(:,i),'Marker',marks{i},'LineWidth',2,'Color',colors{i},'DisplayName',sprintf('%s-reject',leg{i}));        
        leg1{i}=sprintf('%s-reject',leg{i});
    end
    for i=1:4
        d(i)=plot(xvalues,drop(:,i),'Marker',marks{i},'LineWidth',2,'Color',colors{i},'LineStyle','--','DisplayName',sprintf('%s-drop',leg{i}));
        leg2{i}=sprintf('%s-drop',leg{i});
    end
    ylabel('% of tasks');
    xlabel(xl);
    set(findall(gcf,'type','text'),'fontSize',13)
    set(findobj(gcf,'type','axes'),'fontsize',13)
    
    xlim([min(xvalues)*0.95,max(xvalues)*1.05]);
    ylim([0,100]);
    set(gca,'XScale','log');
    set(gca,'Xtick',xvalues);
    set(gcf,'OuterPosition',[500,500,375,360])
    
    ah1 = gca;
    % Legend at axes 1
    l1=legend(ah1,r,leg1);
    set(l1,'interpreter','none');
    set(l1,'fontsize',9);

    % Block 2
    % Axes handle 2 (unvisible, only for place the second legend)
       ah2=axes('position',get(gca,'position'), 'visible','off');
    % Legend at axes 2
    l2=legend(ah2,d,leg2);
    set(l2,'interpreter','none');
    set(l2,'fontsize',9);
    
end