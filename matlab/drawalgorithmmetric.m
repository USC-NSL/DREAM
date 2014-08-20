function drawalgorithmmetric(alg,folders,folderslabel, t,column)
    subs={};
    for i=1:length(folders)
        subs{i}=sprintf('%s/%s/metrics.csv',folders{i},alg);
    end
    [x,labels]=loadalgorithmmetrics('E:/enl/measurement/DynamicMonitor/output',subs);
    y=extractametric(x,column,column+1);
    drawMetrics(y,labels{2},folderslabel)
    x=importdata(sprintf('%s/%s','E:/enl/measurement/DynamicMonitor/output',subs{1}));
    ylabel(x.colheaders{column+1},'interpreter','none');
    %ylabel('Recall')
    title(sprintf('%s %s',alg,t));       
end