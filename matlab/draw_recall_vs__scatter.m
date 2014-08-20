clear;
load_sketch;
%load_rm;

switchmemindex=3;
hhhweightmeanindex=12;
hhhweightmaxindex=14;

%plot recall vs column
switch_num=4;
recall_col=20;
x_col=hhhweightmeanindex;
series_col=switchmemindex;

figure;
hold all;
colors=jet(length(x)/4);
for j=1:length(x)
    c=colors(floor((j-1)/4)+1,:);
    y=x{j};    
    %indexes=and(y(:,2)==switch_num,or(y(:,1)==256,or(y(:,1)==1024,y(:,1)==4096)));
    indexes=y(:,2)==switch_num;    
    series=unique(y(:,series_col));
    for i=1:length(series)
        seriesi=series(i);
        xseriesi=y(y(:,series_col)==seriesi,:);
        plot(xseriesi(:,x_col),xseriesi(:,recall_col),'*','DisplayName',sprintf('%d',seriesi),'color',c);
    end
    z=y(y(:,3)==0,:);
    z(:,3)=z(:,1)/switch_num;%copy the size of the switch
    z2=z;
    z(1,:)=[];
    plot(z(:,x_col),z(:,recall_col),'-*','DisplayName','Single','color',c);
    %plot(z(:,x_col),z(:,recall_col),'DisplayName',sprintf('Single/%d',switch_num));
    z=z2;
    plot(z(1,x_col),z(1,recall_col),'+','LineWidth',2,'MarkerSize',12,'DisplayName','Real HHH','color',c);
%    draw_metric_multiple_switch(y(indexes,:),series_col,x_col,recall_col)
end


legend off
%legend show

%xlabel('A switch capacity')
xlabel('Average HHH Weight')
%xlabel('Max HHH Weight')
ylabel('Recall');
title(sprintf('Series on Resource for %d switches',switch_num));
