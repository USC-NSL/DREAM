%clear;
%figure;
w=1000;
d=4;
th=0.001*401406680;
folder='_k06';
sums=csvread(sprintf('E:/enl/measurement/DynamicMonitor/output/hh/hh%s/0.001_1000000_a+0-3/HHGroundTruth_0_4_1000/sums.csv',folder));
sums(:,1)=[0:size(sums,1)-1];

folder='_sum_k06';
fid=fopen(sprintf('E:/enl/measurement/DynamicMonitor/output/hh/hh%s/0.001_1000000_a+0-3/CountMinSketch_0_4_%d/hhh.csv',folder,w),'r');
x2=textscan(fid,'%d %s %f','delimiter',',');
fclose(fid);
data=[x2{1},bin2dec(x2{2}),x2{3}];

folder='_k06';
fid=fopen(sprintf('E:/enl/measurement/DynamicMonitor/output/hh/hh%s/0.001_1000000_a+0-3/HHGroundTruth_0_4_1000/hhh.csv',folder),'r');
x2=textscan(fid,'%d %s %f','delimiter',',');
fclose(fid);
g=[x2{1},bin2dec(x2{2}),x2{3}];
p=ismember(data(:,1:2),g(:,1:2),'rows');


precision=[];
hhsum=[];
for i=1:size(sums,1)
    sumsi1=sums(i,1);
    sumsi2=sums(i,2);
    indeces=data(:,1)==sumsi1;
    data_time=double(data(indeces,3));
    hhh_sum=sum(data_time);
    hhsum=[hhsum, hhh_sum];
    y=data_time-th;
    y(y<0)=1; %because sketch is long
    %s=sumsi2-hhh_sum;
    s=sumsi2;
    precision=[precision;[data_time,max(0,1-(1./(w.*(y./s))).^d),p(indeces)],sumsi2*ones(size(data_time,1),1)];
end
precision=sortrows(precision,1);
x=sortrows([max(1,(precision(:,1)-th))./precision(:,4),precision],1);
plot(x(:,1),x(:,3))
hold all;

%hx=linspace(min(x(:,1)),max(x(:,1)),1001);
hx=logspace(log10(min(x(:,1))),log10(max(x(:,1))),51);
for i=1:length(hx)-1,indeces=and(x(:,1)>=hx(i),x(:,1)<hx(i+1)); t(i)=sum(x(indeces,4))/sum(indeces); end
tnan=find(~isnan(t));
plot((hx(tnan)+hx(tnan+1))/2,t(tnan),'-')
set(gca,'XScale','log')