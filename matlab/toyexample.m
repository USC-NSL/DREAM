marks={'-k','--b','-.g',':r'};
xlabels=[256,512,1024,2048]; 
figure; 
hold all;
j=34;
for i1=1:length(xlabels);
     i=xlabels(i1);
     x=csvread(sprintf('E:/enl/measurement/DynamicMonitor/output/thhtoyexample/all/%d_0011/%d/HHHMetrics.csv',i,j),1,0);
     plot(x(:,1),smooth(x(:,4)),marks{i1},'LineWidth',2);
end
legend({'256','512','1024','2048'});
set(findall(gcf,'type','text'),'fontSize',14')
set(findobj('type','axes'),'fontsize',14)
xlabel('Time (s)');
ylabel('Recall');
ylim([0,1]);

figure; 
hold all; 
x1=csvread(sprintf('E:/enl/measurement/DynamicMonitor/output/thhtoyexample/all/%d_0011/%d/HHHMetrics.csv',256,34),1,0);
x2=csvread(sprintf('E:/enl/measurement/DynamicMonitor/output/thhtoyexample/all/%d_0011/%d/HHHMetrics.csv',256,60),1,0);
plot(x1(:,1),smooth(x1(:,4)),marks{1},'LineWidth',2);
plot(x2(:,1),smooth(x2(:,4)),marks{2},'LineWidth',2);
legend({'Switch 1','Switch 2'});
set(findall(gcf,'type','text'),'fontSize',14')
set(findobj('type','axes'),'fontsize',14)
xlabel('Time (s)');
ylabel('Recall');
ylim([0,1]);
