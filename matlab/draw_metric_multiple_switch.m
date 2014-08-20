function draw_metric_multiple_switch(x,series_column,x_column,metric_column,std_min_column,max_column)
    if (nargin<6)
        max_column=0;
        if (nargin<5)
            std_min_column=0;
        end
    end
    series=unique(x(:,series_column));
    figure;
    hold all;
    for i=1:length(series)
        seriesi=series(i);
        xseriesi=x(x(:,series_column)==seriesi,:);
        if (max_column>0)
            errorbar(xseriesi(:,x_column),xseriesi(:,metric_column),xseriesi(:,metric_column)-xseriesi(:,std_min_column),xseriesi(:,max_column)-xseriesi(:,metric_column),'DisplayName',sprintf('%d',seriesi));
        elseif (std_min_column>0)
            errorbar(xseriesi(:,x_column),xseriesi(:,metric_column),xseriesi(:,std_min_column),'DisplayName',sprintf('%d',seriesi));
        else
            plot(xseriesi(:,x_column),xseriesi(:,metric_column),'-*','DisplayName',sprintf('%d',seriesi));
        end
    end
end