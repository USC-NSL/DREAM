% clear
% task_num=2048;
% warmup_low=300;
% warmup_high=2750-300;
% path='E:/enl/measurement/DynamicMonitor/output/switchsize_2/16k'; equal_util_sum=load_equal_sum_share(sprintf('%s/equal',path),warmup_low,warmup_high);diagram_all_folders2
% f1=f;
% path='E:/enl/measurement/DynamicMonitor/output/oracle/16k';diagram_all_folders2
% f1=[f1;f];
% 
% path='E:/enl/measurement/DynamicMonitor/output/switchsize_2/32k'; equal_util_sum=load_equal_sum_share(sprintf('%s/equal',path),warmup_low,warmup_high);diagram_all_folders2
% f2=f;
% path='E:/enl/measurement/DynamicMonitor/output/oracle/32k';diagram_all_folders2
% f2=[f2;f];
% 
% path='E:/enl/measurement/DynamicMonitor/output/switchsize_2/48k'; equal_util_sum=load_equal_sum_share(sprintf('%s/equal',path),warmup_low,warmup_high);diagram_all_folders2
% f3=f;
% path='E:/enl/measurement/DynamicMonitor/output/oracle/48k';diagram_all_folders2
% f3=[f3;f];
% 
% path='E:/enl/measurement/DynamicMonitor/output/switchsize_2/64k'; equal_util_sum=load_equal_sum_share(sprintf('%s/equal',path),warmup_low,warmup_high);diagram_all_folders2
% f4=f;
% path='E:/enl/measurement/DynamicMonitor/output/oracle/64k';diagram_all_folders2
% f4=[f4;f];


clear
task_num=2048;
warmup_low=300;
warmup_high=2750-300;
path='E:/enl/measurement/DynamicMonitor/output/tcamswitchsize/512'; equal_util_sum=load_equal_sum_share(sprintf('%s/equal',path),warmup_low,warmup_high);diagram_all_folders2
f1=f;
path='E:/enl/measurement/DynamicMonitor/output/tcam_oracle/512';diagram_all_folders2
f1=[f1;f];

path='E:/enl/measurement/DynamicMonitor/output/tcamswitchsize/1024'; equal_util_sum=load_equal_sum_share(sprintf('%s/equal',path),warmup_low,warmup_high);diagram_all_folders2
f2=f;
path='E:/enl/measurement/DynamicMonitor/output/tcam_oracle/1024';diagram_all_folders2
f2=[f2;f];

path='E:/enl/measurement/DynamicMonitor/output/tcamswitchsize/1536'; equal_util_sum=load_equal_sum_share(sprintf('%s/equal',path),warmup_low,warmup_high);diagram_all_folders2
f3=f;
path='E:/enl/measurement/DynamicMonitor/output/tcam_oracle/1536';diagram_all_folders2
f3=[f3;f];

path='E:/enl/measurement/DynamicMonitor/output/tcamswitchsize/2048'; equal_util_sum=load_equal_sum_share(sprintf('%s/equal',path),warmup_low,warmup_high);diagram_all_folders2
f4=f;
path='E:/enl/measurement/DynamicMonitor/output/tcam_oracle/2048';diagram_all_folders2
f4=[f4;f];