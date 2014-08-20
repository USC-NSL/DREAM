k=80;
[h,hx]=hist(ssj(:,2)-g(:,2),100);
hx2=hx(hx>0);
h2=1-0.5*exp(-k*(exp(hx2)-1).^2./(2+4*exp(hx2)));
figure;
subplot(2,1,1);
hold all;
plot(hx,cumsum(h)/sum(h));
plot(hx2,h2);

[h,hx]=hist(-ssj(:,2)+g(:,2),100);
hx2=hx(hx>0);
h2=1-0.5*exp(-k*(exp(hx2)-1).^2./(2+4*exp(hx2)));
subplot(2,1,2);
hold all;
plot(hx,cumsum(h)/sum(h));
plot(hx2,h2);

