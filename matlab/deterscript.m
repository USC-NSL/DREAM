path1='E:/enl/measurement/DynamicMonitor/output/deter256/changesize/realchange/256';
deter256
f1_r=f;
path1='E:/enl/measurement/DynamicMonitor/output/deter256/changesize/realchange/512';
deter256
f2_r=f;
path1='E:/enl/measurement/DynamicMonitor/output/deter256/changesize/realchange/768';
deter256
f3_r=f;
path1='E:/enl/measurement/DynamicMonitor/output/deter256/changesize/realchange/1024';
deter256
f4_r=f;

path1='E:/enl/measurement/DynamicMonitor/output/deter256/changesize/sim/256';
deter256
f1_s=f;
path1='E:/enl/measurement/DynamicMonitor/output/deter256/changesize/sim/512';
deter256
f2_s=f;
path1='E:/enl/measurement/DynamicMonitor/output/deter256/changesize/sim/768';
deter256
f3_s=f;
path1='E:/enl/measurement/DynamicMonitor/output/deter256/changesize/sim/1024';
deter256
f4_s=f;

load('data/deter/changesim.mat');
f1_a=f1([1 2 4 6],:);
f2_a=f2([1 2 4 6],:);
f3_a=f3([1 2 4 6],:);
f4_a=f4([1 2 4 6],:);