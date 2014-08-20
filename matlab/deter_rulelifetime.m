x=csvread('E:/enl/measurement/DynamicMonitor/output/deter256/r2/512/dream_h5/log_avgdur.txt');
index=and(x(:,1)>=307,x(:,1)<2475);
[h1,hx1]=hist(1-x(index,2),10000);

data=draw_profiler2('E:/enl/measurement/DynamicMonitor/output/deter256/r2/512/dream_h5/profile_EpochProcedure.csv');
close all;
t=unique(str2double(data.textdata(:,1)));
t2=str2double(data.textdata(:,1));
dursum=[];for i=1:length(t), dursum(i)=sum(data.data(t2==t(i))); end
[h2,hx2]=hist(dursum/1000000000,10000);

plot(hx1*1000,cumsum(h1)/sum(h1),'LineWidth',2)
hold all;
plot(hx2*1000,cumsum(h2)/sum(h2),'LineWidth',2)
legend('1-Rule Lifetime','Fetch->Save Barrier');
xlabel('Time (ms)')
ylabel('CDF');
set(findall(gcf,'type','text'),'fontSize',14')
set(findobj('type','axes'),'fontsize',14)