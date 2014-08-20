mse=zeros(4,4);
meanrelative=zeros(4,4);
files=[1,2,13,30]; 
ks=[10,20,40,80]; 
path='E:/enl/measurement/DynamicMonitor/output/entropytest';
for f=1:length(files);
    figure;
    g=csvread(sprintf('%s/groundtruth/%d/1/entropy.csv',path,files(f))); 
    n=csvread(sprintf('%s/groundtruth/%d/1/num.csv',path,files(f)));
    n0=log(n(:,2));
    gnorm=g(:,2)./n0;
    plot(g(:,1),g(:,2),'DisplayName','GroundTruth','LineWidth',2,'Color','black'); 
    hold all; 
    for i=1:length(ks), 
        ssj=csvread(sprintf('%s/stableskewj/%d/a_0999,k%d/1/entropy.csv',path,files(f),ks(i)));  
        if (ks(i)>=40)
            plot(ssj(:,1),ssj(:,2),'DisplayName',sprintf('%d',ks(i)));
        end
        mse(f,i)=mean((g(:,2)-ssj(:,2)).^2)/(mean(g(:,2))*mean(ssj(:,2))); 
        hnorm=ssj(:,2)./n0;
        meanrelative(f,i)=mean(abs(gnorm-hnorm)./gnorm);
    end;
    ylim([0,max(n0)]);
    legend show;
    xlabel('Time');
    ylabel('Entropy');
end