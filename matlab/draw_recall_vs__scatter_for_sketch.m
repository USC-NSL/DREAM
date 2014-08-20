clear;
load_sketch_skew;
%load_sketch_ra;

hhhweightmeanindex=12;
hhhweightmaxindex=14;

%plot recall vs column
recall_col=20;
x_col=hhhweightmeanindex;

figure;
hold all;
colors=jet(length(x)/4);
for j=1:length(x)
    c=colors(floor((j-1)/4)+1,:);
    y=x{j};
    z=y(y(:,2)~=1,:);
    resource_usage=calculate_sketch_memory(z(:,1),z(:,3),z(:,2));
    [resource_usage,resource_usage_i]=sort(resource_usage);
    z=z(resource_usage_i,:);
    real_index=y(:,2)==1;
    plot(z(:,x_col),z(:,recall_col),'-*','DisplayName','Single','color',c);
    z=y;
    plot(z(real_index,x_col),z(real_index,recall_col),'+','LineWidth',2,'MarkerSize',12,'DisplayName','Real HHH','color',c);
end


legend off
%legend show

%xlabel('A switch capacity')
xlabel('Average HHH Weight')
%xlabel('Max HHH Weight')
ylabel('Recall');
