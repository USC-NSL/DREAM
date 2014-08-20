function c=hhh_corr(path,ext,func1,func2)
%    path='E:/enl/measurement/DynamicMonitor/outputtemp2/0.010_1000000_a+0-3/SingleSwitch_512_1';
    x=load_hhh(sprintf('%s/hhh.csv',path));
    m=cellfun(ext,x,'UniformOutput',false);
    m2=cellfun((@(x) x(1)),m);
    %display(path);
    metrics=csvread(sprintf('%s/HHHMetrics.csv',path),1,0);
    c=corr(feval(func2,metrics(:,10)),feval(func1,m2(1:size(metrics,1))));
end