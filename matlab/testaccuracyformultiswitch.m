for i=[141:160]
    figure;
    a=csvread(sprintf('%s/threshold_2_notallpoor_weight1_ewma/%d/acc.csv',path,i));
    plot(a(:,1),a(:,2))
    hold all;
    x=csvread(sprintf('%s/threshold_2_notallpoor_weight1_ewma/%d/HHHMetrics.csv',path,i),1,0);
    plot(x(:,1),x(:,3))
end