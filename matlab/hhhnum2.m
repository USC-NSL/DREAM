clear;
z=[];
k=1;
%indexes=[0:2:8 10 20];
%indexes=[0:2:8];
indexes=[4:2:16];
for i=indexes
    if i==10
        x=load_hhh('E:/enl/measurement/DynamicMonitor/outputserver/output00/0.010_1000000_a+0-3/HHHPredictor2_1024_1/hhh.csv');
    else
        %x=load_hhh(sprintf('E:/enl/measurement/DynamicMonitor/outputserver/output00_rm_%03.f_5/0.010_1000000_a+0-3/HHHPredictor2_1024_1/hhh.csv',i));
        %x=load_hhh(sprintf('E:/enl/measurement/DynamicMonitor/outputserver/output00_ra_%02.f/0.010_1000000_a+0-3/HHHPredictor2_1024_1/hhh.csv',i));
        x=load_hhh(sprintf('E:/enl/measurement/DynamicMonitor/outputserver/output00_k%02.f/0.010_1000000_a+0-3/HHHPredictor2_1024_1/hhh.csv',i));
    end
    z(k)=mean(cellfun('length',x));
    k=k+1;
end

figure;
plot((indexes)/10,z,'-*');