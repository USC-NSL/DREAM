function [h,he]=draw_bar(sat,xl,yl,xvalues,leg,line_styles,colors,xdiff,line_widths)
    sat=sat*100;
    if (nargin<5)
        leg={'DREAM';'Equal';'Fixed'};
        line_styles={'-','--','-.'};
        colors={'black';'blue';'red'};
        xdiff=[-0.2,0,0.2];
        line_widths=[2 2 2];
    end
    %c=gray(length(leg)+1);
    index=[1:length(leg)];
    figure;
    hold all;
    %err(:,:,1)=[sat(:,:,3)-sat(:,:,1)];
    %err(:,:,2)=[sat(:,:,2)-sat(:,:,3)];
    %[h,he]=barwitherr(err,sat(:,:,3));
    
    for i=1:size(sat,2)
        plot([0,1],[1,2],line_styles{i},'LineWidth',line_widths(i),'Color',colors{i},'Visible','off');
    end
    l=legend(leg(index));
    set(l,'interpreter','none');
    for i=1:size(sat,2)
        for j=1:length(xvalues)
           plot([j+xdiff(i),j+xdiff(i)],[sat(j,i,1),sat(j,i,3)],line_styles{i},'LineWidth',line_widths(i),'Color',colors{i});
        end      
    end
    
    for i=1:size(sat,2)
        for j=1:length(xvalues)
           plot(j+xdiff(i),sat(j,i,1),'^','Color',colors{i});
        end      
    end
    
    for i=1:size(sat,2)
        for j=1:length(xvalues)
           plot(j+xdiff(i),sat(j,i,3),'s','Color',colors{i});
        end      
    end
    
    set(gca,'XTick',[1:size(sat,1)]);
    set(gca,'XTickLabel',arrayfun(@(x) sprintf('%d',x),xvalues,'uni',false))
    xlabel(xl);
    ylabel(yl);

    set(findall(gcf,'type','text'),'fontSize',13')
    set(findobj(gcf, 'type','axes'),'fontsize',13)

    set(l,'fontsize',10);
    xlim([0.5,size(sat,1)+0.5]);
    ylim([0,100]);
    %colormap(c((length(leg)/3+1):(length(leg)+1),:));
    set(gcf,'OuterPosition',[500,500,375,360])
end