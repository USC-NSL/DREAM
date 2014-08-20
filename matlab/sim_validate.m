clear
%[sat1,rej1,drop1]=load_estimate2('E:/enl/measurement/DynamicMonitor/output/deter256/hhsize/simlevel4arrival64spt/sim/',[512,1024,2048,4096]); 
%[sat,rej,drop]=load_estimate2('E:/enl/measurement/DynamicMonitor/output/deter256/hhsize/simlevel4arrival64spt/realhh/',[512,1024,2048,4096]); 

 %[sat1,rej1,drop1]=load_estimate2('E:/enl/measurement/DynamicMonitor/output/deter256/hhhsize/simlevel4arrival64spt/sim/',[512,1024,2048,4096]); 
 %[sat,rej,drop]=load_estimate2('E:/enl/measurement/DynamicMonitor/output/deter256/hhhsize/simlevel4arrival64spt/realhhh/',[512,1024,2048,4096]); 

[sat1,rej1,drop1]=load_estimate2('E:/enl/measurement/DynamicMonitor/output/deter256/changesize/simlevel4arrival64spt/sim/',[512,1024,2048,4096]); 
[sat,rej,drop]=load_estimate2('E:/enl/measurement/DynamicMonitor/output/deter256/changesize/simlevel4arrival64spt/realchange/',[512,1024,2048,4096]); 

%[sat1,rej1,drop1]=load_estimate2('E:/enl/measurement/DynamicMonitor/output/deter256/combinesize/simlevel4arrival64spt/sim/',[512,1024,2048,4096]); 
%[sat,rej,drop]=load_estimate2('E:/enl/measurement/DynamicMonitor/output/deter256/combinesize/simlevel4arrival64spt/realcombine/',[512,1024,2048,4096]); 

rejall=[];dropall=[];satall=[]; 
for i=1:3;
    satall(:,2*i-1,:)=sat(:,i,:); 
    satall(:,2*i,:)=sat1(:,i,:); 
    rejall(:,2*i-1,:)=rej(:,i,:); 
    rejall(:,2*i,:)=rej1(:,i,:); 
    dropall(:,2*i-1,:)=drop(:,i,:); 
    dropall(:,2*i,:)=drop1(:,i,:); 
end
xvalues=[512,1024,2048,4096];
draw_bar(satall,'Switch capacity','Satisfaction',xvalues,{'DREAM','DREAM_s','Equal','Equal_s','Fixed','Fixed_s'},{'-','--','-.','-','--','-.'},{'black','black','blue','blue','red','red'},[-0.30 -0.20 -0.05 0.05 0.20 0.30],[2 1 2 1 2 1]);
draw_droprej2(dropall,rejall,'Switch capacity',xvalues,{'DREAM-reject','DREAM_s-reject','Fixed-reject','Fixed_s-reject','DREAM-drop','DREAM_s-drop'},{'-sk','--sk','-vr','--vr','-+k','--+k'});