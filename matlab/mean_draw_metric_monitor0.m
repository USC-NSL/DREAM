clear x;
x(:,:,1)=load_metric('E:/enl/measurement/DynamicMonitor/outputserver/output00/0.010_1000000_a+0-3/MonitorMetrics.csv');
x(:,:,2)=load_metric('E:/enl/measurement/DynamicMonitor/outputserver/output01/0.010_1000000_a+0-3/MonitorMetrics.csv');
x(:,:,3)=load_metric('E:/enl/measurement/DynamicMonitor/outputserver/output02/0.010_1000000_a+0-3/MonitorMetrics.csv');
x(:,:,4)=load_metric('E:/enl/measurement/DynamicMonitor/outputserver/output03/0.010_1000000_a+0-3/MonitorMetrics.csv');
x(:,:,5)=load_metric('E:/enl/measurement/DynamicMonitor/outputserver/output04/0.010_1000000_a+0-3/MonitorMetrics.csv');
y=mean(x,3);
ymin=min(x,[],3);
ymax=max(x,[],3);
col=6;

draw_metric_multiple_switch([y(y(:,2)==4,:),ymin(y(:,2)==4,:),ymax(y(:,2)==4,:)],3,1,col,size(y,2)+col,2*size(y,2)+col)

z=y(y(:,3)==0,:);
z(1,:)=[];
cap=1024;plot([min(y(y(:,2)==4,1)),max(y(y(:,2)==4,1))],repmat(z(z(:,1)==cap,col),1,2),'DisplayName',sprintf('Single,%d',cap));
cap=512;plot([min(y(y(:,2)==4,1)),max(y(y(:,2)==4,1))],repmat(z(z(:,1)==cap,col),1,2),'DisplayName',sprintf('Single,%d',cap));
cap=256;plot([min(y(y(:,2)==4,1)),max(y(y(:,2)==4,1))],repmat(z(z(:,1)==cap,col),1,2),'DisplayName',sprintf('Single,%d',cap));
legend show
set(gca,'XScale','log')
set(gca,'XScale','log')
ylabel('Number of monitors')
xlabel('Number of IP regions')
