function [data,g,p]=load_cm_hh(folder,w)

    fid=fopen(sprintf('E:/enl/measurement/DynamicMonitor/output/hh/hh%s/0.001_1000000_a+0-3/CountMinSketch_0_4_%d/hhh.csv',folder,w),'r');
    x2=textscan(fid,'%d %s %f','delimiter',',');
    fclose(fid);
    data=[x2{1},bin2dec(x2{2}),x2{3}];
    
    fid=fopen(sprintf('E:/enl/measurement/DynamicMonitor/output/hh/hh%s/0.001_1000000_a+0-3/HHGroundTruth_0_4_1000/hhh.csv',folder),'r');
    x2=textscan(fid,'%d %s %f','delimiter',',');
    fclose(fid);
    g=[x2{1},bin2dec(x2{2}),x2{3}];
    p=ismember(data(:,1:2),g(:,1:2),'rows');    
end