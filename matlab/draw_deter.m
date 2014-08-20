%marks={'-vb','-sk','-sr','-+g','-^c','-sm','-oy','-+g'};
marks={'--ob','--+k','--sr','--^g','--^c','--sm','--oy','--+g'};
ylabels={'Reject Tasks','Drop Tasks','Drop Age','Satisfaction','Utilization'};
leg={'DREAM';'Equal';'Fixed_32'};
index=[1,2,3];
fig=zeros(size(f1,2));
for c=1:4;
 %   figure; 
%    hold all; 
    figure(c);
    for i=index; 
        hold all; 
        plot(xvalues,[f1(i,c) f2(i,c) f3(i,c) f4(i,c)],marks{i},'LineWidth',2,'MarkerSize',10); 
    end;
    l=legend(leg{index});
    set(l,'interpreter','none');
    ylabel(ylabels{c})
    set(findall(gcf,'type','text'),'fontSize',14')
    set(findobj('type','axes'),'fontsize',14)
    set(l,'fontsize',12);
    xlabel('Switch Capacity');
    ylim([0,1]);
    set(gca,'XTick',xvalues);
     xlim([min(xvalues)*0.95,max(xvalues)*1.05])
end

% f1=f1_r;f2=f2_r;f3=f3_r;f4=f4_r;
% f1=f1_s;f2=f2_s;f3=f3_s;f4=f4_s;
% f1=f1_a;f2=f2_a;f3=f3_a;f4=f4_a;