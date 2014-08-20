function draw_graphsyy(f1,f2,f3,f4,xvalues,xl)
marks={'v','+','s','o'};
c={[0,0,0],[0,0,1]};
ylabels={'Reject Tasks','Drop Tasks','Drop Age','Satisfaction','Utilization'};
%leg={'DREAM_old';'DREAM';'Equal';'Fixed_8';'Fixed_4';'Fixed_32';'Fixed_16'};
%index=[2,3,6];

leg={'DREAM';'Equal';'Fixed_32'};
index=[1:3];

%index=[5,4,7,6];
figure;
for i2=1:min(length(index),size(f1,1)); 
    i=index(i2);
    [ax,h1,h2]=plotyy(xvalues,[f1(i,1) f2(i,1) f3(i,1) f4(i,1)],xvalues,[f1(i,4) f2(i,4) f3(i,4) f4(i,4)],'plot','plot');
    hold(ax(1),'on')
    hold(ax(2),'on')
    h0(i2)=plot(xvalues,[f1(i,1) f2(i,1) f3(i,1) f4(i,1)],'Marker',marks{i2},'MarkerSize',9,'Color','black','LineStyle','none');
    set(h1,'Marker',marks{i2});
    set(h2,'Marker',marks{i2});
    set(h1,'MarkerSize',9)
    set(h2,'MarkerSize',9)
    set(h1,'Color',c{1});
    set(h2,'Color',c{2});
    set(h1,'LineWidth',2)
    set(h1,'LineStyle','--');
    set(h2,'LineWidth',2)
    set(ax(1),'Ylim',[0,1])
    set(ax(2),'Ylim',[0,1])
    set(ax(1),'YTick',0:0.2:1) 
    set(ax(2),'YTick',0:0.2:1) 
    set(ax,{'ycolor'},{c{1};c{2}})
    set(ax(1)','XTick',xvalues);
    set(ax(2)','XTick',xvalues);
    set(ax(1)','Xlim',[min(xvalues)*0.95,max(xvalues)*1.05]);
    set(ax(2)','Xlim',[min(xvalues)*0.95,max(xvalues)*1.05]);
    set(ax(1),'Position', [0.13+.04 0.11+.08 0.775-.08 0.815-.08]);
    set(ax(2),'Position', [0.13+.04 0.11+.08 0.775-.08 0.815-.08]);    
end;
l=legend(h0,leg{index});
set(l,'interpreter','none');
set(l,'Location','Best');
xlabel(xl);
set(get(ax(1),'Ylabel'),'String',ylabels{1}) 
set(get(ax(2),'Ylabel'),'String',ylabels{4})
set(findall(gcf,'type','text'),'fontSize',14')
set(findobj('type','axes'),'fontsize',14)
set(l,'fontsize',12);
for i=1:length(h0)
    set(h0(i),'Visible','off');
end
end