x={};
indexes=[0:2:8];
cols=[12 14];
max_data=0.01*[401421638, 427186609, 429176540, 421694749];
base=1;
for i=indexes
    path=sprintf('E:/enl/measurement/DynamicMonitor/outputserver/output00_ra_%02.f',i);
    if i==0
        path='E:/enl/measurement/DynamicMonitor/outputserver/output00';
    end
    x{length(x)+1}=load_metric(sprintf('%s/0.010_1000000_a+0-3/HHHMetrics.csv',path));
    x{length(x)+1}=load_metric(sprintf('%s/0.010_1000000_a+3-6/HHHMetrics.csv',path));
    x{length(x)+1}=load_metric(sprintf('%s/0.010_1000000_a+6-9/HHHMetrics.csv',path));
    x{length(x)+1}=load_metric(sprintf('%s/0.010_1000000_a+9-12/HHHMetrics.csv',path));
    x{base}(:,cols)=x{base}(:,cols)./max_data(1);
    x{base+1}(:,cols)=x{base+1}(:,cols)./max_data(2);
    x{base+2}(:,cols)=x{base+2}(:,cols)./max_data(3);
    x{base+3}(:,cols)=x{base+3}(:,cols)./max_data(4);
    base=base+4;
end

%max_data=0.01*[1.4688e+005, 1.4281e+005, 1.5795e+005, 1.4944e+005];


%  base=1;
% x{base}(:,cols)=x{base}(:,cols)./max_data(1);
% x{base+1}(:,cols)=x{base+1}(:,cols)./max_data(2);
% x{base+2}(:,cols)=x{base+2}(:,cols)./max_data(3);
% x{base+3}(:,cols)=x{base+3}(:,cols)./max_data(4);
% % 
% % max_data=0.01*[1.4736e+006, 1.5103e+006, 1.4352e+006, 1.5775e+006];
%  base=5;
% x{base}(:,cols)=x{base}(:,cols)./max_data(1);
% x{base+1}(:,cols)=x{base+1}(:,cols)./max_data(2);
% x{base+2}(:,cols)=x{base+2}(:,cols)./max_data(3);
% x{base+3}(:,cols)=x{base+3}(:,cols)./max_data(4);
% 
% %max_data=0.01*[401421638, 427186609, 429176540, 421694749];
% base=9;
% x{base}(:,cols)=x{base}(:,cols)./max_data(1);
% x{base+1}(:,cols)=x{base+1}(:,cols)./max_data(2);
% x{base+2}(:,cols)=x{base+2}(:,cols)./max_data(3);
% x{base+3}(:,cols)=x{base+3}(:,cols)./max_data(4);
% 
%  %max_data=0.01*[1.009e+015, 1.4542e+015,  1.2119e+015,  1.2651e+015];
%  %max_data=0.01*[1.4688e+005, 1.4281e+005, 1.5795e+005, 1.4944e+005];
% base=13;
% x{base}(:,cols)=x{base}(:,cols)./max_data(1);
% x{base+1}(:,cols)=x{base+1}(:,cols)./max_data(2);
% x{base+2}(:,cols)=x{base+2}(:,cols)./max_data(3);
% x{base+3}(:,cols)=x{base+3}(:,cols)./max_data(4);
% 
% base=17;
% x{base}(:,cols)=x{base}(:,cols)./max_data(1);
% x{base+1}(:,cols)=x{base+1}(:,cols)./max_data(2);
% x{base+2}(:,cols)=x{base+2}(:,cols)./max_data(3);
% x{base+3}(:,cols)=x{base+3}(:,cols)./max_data(4);
% 
% base=21;
% x{base}(:,cols)=x{base}(:,cols)./max_data(1);
% x{base+1}(:,cols)=x{base+1}(:,cols)./max_data(2);
% x{base+2}(:,cols)=x{base+2}(:,cols)./max_data(3);
% x{base+3}(:,cols)=x{base+3}(:,cols)./max_data(4);