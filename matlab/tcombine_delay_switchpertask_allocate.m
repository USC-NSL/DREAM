errorbar(xvalues,data32(1,:),data32(1,:)-data32(2,:),data32(3,:)-data32(1,:),'LineWidth',2,'Color','black')
set(gca,'Xtick',xvalues);
ylabel('Allocation delay (ms)');
xlabel('Switch per task');
set(gca,'XTick',xvalues);

set(findall(gcf,'type','text'),'fontSize',13')
set(findobj(gcf, 'type','axes'),'fontsize',13)

xlim([min(xvalues)*0.95,max(xvalues)*1.05]);
set(gcf,'OuterPosition',[500,500,375,360])

