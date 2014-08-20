%load hhh
%load timeseries
function [r,hhh]=load_recall_hhh_timeseries(path)
    recall_col=10;
    x=csvread(sprintf('%s/HHHMetrics.csv',path),1,0);
    r=x(:,recall_col);
    hhh=load_hhh(sprintf('%s/hhh.csv',path));
end