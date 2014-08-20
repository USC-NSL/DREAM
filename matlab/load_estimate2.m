function [sat,rej,drop]=load_estimate2(path,xvalues)
%path='E:/enl/measurement/DynamicMonitor/output/deter256/changesize/simlevel4arrival64spt/realchange/';
for i=1:length(xvalues)
    xvaluesi=xvalues(i);
    [sat(i,:,:),rej(i,:),drop(i,:)]=load_estimate(sprintf('%s/%d',path,xvaluesi));
end
end