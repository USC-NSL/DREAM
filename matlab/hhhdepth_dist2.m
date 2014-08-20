figure; 
hold all;
z=[];
k=1;
%indexes=[0:2:8 10 20];
indexes=[0:2:8];
%indexes=[4:2:16];
for i=indexes
    if i==0
        x=load_hhh('E:/enl/measurement/DynamicMonitor/outputserver/output00/0.010_1000000_a+0-3/HHHPredictor2_1024_1/hhh.csv');
    else
        %x=load_hhh(sprintf('E:/enl/measurement/DynamicMonitor/outputserver/output00_rm_%03.f_5/0.010_1000000_a+0-3/HHHPredictor2_1024_1/hhh.csv',i));
        x=load_hhh(sprintf('E:/enl/measurement/DynamicMonitor/outputserver/output00_ra_%02.f/0.010_1000000_a+0-3/HHHPredictor2_1024_1/hhh.csv',i));
%        x=load_hhh(sprintf('E:/enl/measurement/DynamicMonitor/outputserver/output00_k%02.f/0.010_1000000_a+0-3/HHHPredictor2_1024_1/hhh.csv',i));
    end
    h=zeros(1,33);    
    for j=1:length(x)
        xx=x{j};
        for l=1:33
           h(l)=h(l)+sum(32-xx(:,2)==l-1); 
        end
    end
    plot([0:32],h,'DisplayName',sprintf('%.1f',i/10));
    z(k)=h*[0:32]'./sum(h);
    %display(sprintf('%d,%f',i,z(k)));
    k=k+1;
end
legend show

figure;
plot(indexes/10,z);