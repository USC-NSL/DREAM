[sat(1,:,:),rej(1,:),drop(1,:)]=load_real('E:/enl/measurement/DynamicMonitor/output/tarrival/256',0.6);
[sat(2,:,:),rej(2,:),drop(2,:)]=load_real('E:/enl/measurement/DynamicMonitor/output/tarrival/256',0.7);
[sat(3,:,:),rej(3,:),drop(3,:)]=load_real('E:/enl/measurement/DynamicMonitor/output/tarrival/256',0.8);
[sat(4,:,:),rej(4,:),drop(4,:)]=load_real('E:/enl/measurement/DynamicMonitor/output/tarrival/256',0.9);

[sat(1,1,:),rej(1,1),drop(1,1)]=load_real('E:/enl/measurement/DynamicMonitor/output/taccuracy/60',0.6,{'dream_h5f'});
[sat(2,1,:),rej(2,1),drop(2,1)]=load_real('E:/enl/measurement/DynamicMonitor/output/taccuracy/70',0.7,{'dream_h5f'});
[sat(3,1,:),rej(3,1),drop(3,1)]=load_real('E:/enl/measurement/DynamicMonitor/output/tarrival/256',0.8,{'dream_h5f'});
[sat(4,1,:),rej(4,1),drop(4,1)]=load_real('E:/enl/measurement/DynamicMonitor/output/taccuracy/90',0.9,{'dream_h5f'});