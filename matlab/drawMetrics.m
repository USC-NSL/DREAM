function drawMetrics(x,xlabels,labels)    
    figure;
    xindex=1:2:size(x,2);
    h=bar(x(:,xindex));
    hold on;
    lines=size(x,2)/2;
    xsize=size(x,1);
    step=1/(lines+1);
    for i =1:lines
        errorx(:,i)=mean(get(get(h(i),'children'),'xdata'),1);
    end
    %errorx=repmat((1:xsize)',1,lines)+repmat(((1:lines)*step-0.5),xsize,1);
    errorbar(errorx, x(:,xindex) , sqrt(x(:,xindex+1)), 'k', 'Marker', 'none', 'LineStyle', 'none', 'LineWidth',2);
    xlabel('Epoch size (ms)')
    set(gca,'XTickLabel',xlabels)
    l=legend(labels,'Location','SouthEastOutside');
    set(l,'interpreter','none');
    axis([0.5 size(x,1)+0.5 0 max([1 max(max(x(:,xindex)))])])
end