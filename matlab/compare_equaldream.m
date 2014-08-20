maxlifetime=299;
vd=[];
ve=[];
index=69:191;
rej=[];
hhs=[];
for i=index, 
    try, 
        xd=csvread(sprintf('%s/dream_h5f/%d/acc.csv',path,i)); 
        xe=csvread(sprintf('%s/equal/%d/acc.csv',path,i)); 
        if (length(xd)==maxlifetime)
            vd=[sum(xd(:,2)>=0.8)/size(xd,1);vd];
            ve=[sum(xe(:,2)>=0.8)/size(xe,1);ve];
            hhs=[size(importdata(sprintf('%s/../../groundtruth/%d/hhh.csv',path,i)),1);hhs];
        end
    catch e;
        rej=[rej;i];
    end;     
end ;
for i=rej
    index(index==i)=[];    
end
[h,hx]=hist((vd-ve),100);
plot(hx,cumsum(h)/sum(h));
xd=csvread(sprintf('%s/dream_h5f/share.csv',path),1,0);
xe=csvread(sprintf('%s/equal/share.csv',path),1,0);
vdr=zeros(length(index),1);i=1; for ui=index, vdr(i)=mean(xd(xd(:,2)==ui,4)); i=i+1; end
ver=zeros(length(index),1);i=1; for ui=index, ver(i)=mean(xe(xe(:,2)==ui,4)); i=i+1; end
corr(vd-ve,vdr-ver);