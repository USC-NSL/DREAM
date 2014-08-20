function entropy_accuracy2(k,g,ssj)
[h,hx]=hist(ssj(:,2)-g(:,2),100);

%figure;
hold all;

positive_index=hx>=0;
hx2=hx(positive_index);
negative_index=hx<0;
hx3=-hx(negative_index);

plot([-hx3,hx2],[cumsum(h(negative_index))/sum(h),1-(sum(h(negative_index))+cumsum(h(positive_index)))/sum(h)]);
h2=exp(-k*(exp(hx2)-1).^2./(2+4*exp(hx2)));
plot(hx2,h2);

h3=exp(-k*(exp(hx3)-1).^2./(2+4*exp(hx3)));
plot(-hx3,h3);
xlim([-1,1]);

end