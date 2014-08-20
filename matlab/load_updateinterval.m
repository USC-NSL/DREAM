%p='E:/enl/measurement/DynamicMonitor/output/sketch/supdateinterval_arrival512';
p='F:/enl/measurement/tupdateinterval_arrival512';
display('1');
path=sprintf('%s/%d',p,1);
diagram_all_folders2
f1=f;

display('2');
path=sprintf('%s/%d',p,2);
diagram_all_folders2
f2=f;
f2=[f2(1:3,:);fmain;f2(4,:)];

display('4');
path=sprintf('%s/%d',p,4);
diagram_all_folders2
f3=f;

display('8');
path=sprintf('%s/%d',p,8);
diagram_all_folders2
f4=f;

display('16');
path=sprintf('%s/%d',p,16);
diagram_all_folders2
f5=f;