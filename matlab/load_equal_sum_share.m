function s=load_equal_sum_share(path,l,h)
    x=csvread(sprintf('%s/share.csv',path),1,0);
    %//time,task,switch,share,accuracy,accuracy_agg
    s=sum(x(and(x(:,1)>=l,x(:,1)<h),4));
end