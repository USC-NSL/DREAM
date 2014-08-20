goal=[ones(1,30)*100,ones(1,30)*1024,ones(1,30)*300,];
%ex=5;plot(1:length(goal)*ex,[reshape(repmat(goal,ex,1),length(goal)*ex,1)';reshape(repmat(mimd(goal),ex,1),length(goal)*ex,1)';reshape(repmat(aimd(goal),ex,1),length(goal)*ex,1)';reshape(repmat(aiad(goal),ex,1),length(goal)*ex,1)';reshape(repmat(miad(goal),ex,1),length(goal)*ex,1)']); legend('goal','mimd','aimd','aiad','miad');
ex=5;
figure; 
hold all;
plot(1:length(goal)*ex,reshape(repmat(goal,ex,1),length(goal)*ex,1),'DisplayName','Goal');
plot(1:length(goal)*ex,reshape(repmat(mimd(goal),ex,1),length(goal)*ex,1),'DisplayName','MM','LineWidth',2);
plot(1:length(goal)*ex,reshape(repmat(aimd(goal),ex,1),length(goal)*ex,1),'-.','DisplayName','AM','LineWidth',2);
plot(1:length(goal)*ex,reshape(repmat(aiad(goal),ex,1),length(goal)*ex,1),'--','DisplayName','AA','LineWidth',2);
plot(1:length(goal)*ex,reshape(repmat(miad(goal),ex,1),length(goal)*ex,1),':','DisplayName','MA','LineWidth',2);
legend show
set(findall(gcf,'type','text'),'fontSize',14')
set(findobj('type','axes'),'fontsize',14)
xlabel('Time(s)');
ylabel('Resource')