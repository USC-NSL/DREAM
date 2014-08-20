figure;
hold all;

[h,hx]=hist(savedelay,1000);
plot(hx/1e6,cumsum(h)/sum(h),'-k','LineWidth',2,'DisplayName','Save');

[h,hx]=hist(fetchdelay,1000);
plot(hx/1e6,cumsum(h)/sum(h),'--b','LineWidth',2,'DisplayName','Fetch');

[h,hx]=hist(taskdelay,1000);
plot(hx/1e6,cumsum(h)/sum(h),'-.r','LineWidth',2,'DisplayName','Task');

[h,hx]=hist(allocatordelay,1000);
plot(hx/1e6,cumsum(h)/sum(h),':g','LineWidth',2,'DisplayName','Allocator');

l=legend('show');

ylim([0,1])
xlabel('Delay (ms)');
ylabel('CDF');
set(findall(gcf,'type','text'),'fontSize',14)
set(findobj(gcf,'type','axes'),'fontsize',14)
set(gcf,'OuterPosition',[500,500,350,360])
set(l,'fontsize',9);

