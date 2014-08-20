clear
[sat(1,:,:),rej(1,:),drop(1,:)]=load_real('E:/enl/measurement/DynamicMonitor/output/tsize/512',0.8,{'fixed_32','fixed_64','fixed_128','fixed_256'});
[sat(2,:,:),rej(2,:),drop(2,:)]=load_real('E:/enl/measurement/DynamicMonitor/output/tarrival/256',0.8,{'fixed_32','fixed_64','fixed_128','fixed_256'});
[sat(3,:,:),rej(3,:),drop(3,:)]=load_real('E:/enl/measurement/DynamicMonitor/output/tsize/2048',0.8,{'fixed_32','fixed_64','fixed_128','fixed_256'});
[sat(4,:,:),rej(4,:),drop(4,:)]=load_real('E:/enl/measurement/DynamicMonitor/output/tsize/4096',0.8,{'fixed_32','fixed_64','fixed_128','fixed_256'});