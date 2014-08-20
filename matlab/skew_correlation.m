figure;
hold all;
indexes=[4:2:16];
colors=jet(length(indexes));
ci=1;
th=0.01*401421638;
for i=indexes
    path=sprintf('E:/enl/measurement/DynamicMonitor/outputserver/output00_k%02.f/0.010_1000000_a+0-3',i);
    if i==10
        path='E:/enl/measurement/DynamicMonitor/outputserver/output00/0.010_1000000_a+0-3';
    end
    %c=hhh_corr2(path,@mean,(@(x) myewma(x/th,0.8,1)),(@(x) x));
    c=hhh_corr2(path,@std,(@(x) x),(@(x) x));
    plot(c(:,1),c(:,2),'-x','DisplayName',sprintf('%0.1f',i/10),'color',colors(ci,:));
%    plot(c(:,1),c(:,3),'-x','DisplayName',sprintf('%02.f 2',i),'color',colors(ci,:));
    ci=ci+1;
end
xlabel('Switch capacity');
ylabel('Correlation');
legend show;