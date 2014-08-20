function c=hhhmonitor_corr(path,ext,func1,func2)
%    path='E:/enl/measurement/DynamicMonitor/outputtemp2/0.010_1000000_a+0-3/SingleSwitch_512_1';
   % display(path);
    x=csvread(sprintf('%s/HHHMonitorMetrics.csv',path),1,0);
    m2=x(:,11)./x(:,4);
    %display(path);
    metrics=csvread(sprintf('%s/HHHMetrics.csv',path),1,0);
    c=corr(feval(func2,metrics(:,9)),feval(func1,m2(1:size(metrics,1))));
end