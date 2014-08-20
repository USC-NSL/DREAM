function [out,index]=categorize_profiler2(path,keywords,index)
    if (nargin<3)
        x=csvread(sprintf('%s/share.csv',path),1,0);
        warmup_low=max(x(x(:,2)==1,1));
        last_task = max(x(:,2));
        warmup_high=min(x(x(:,2)==last_task,1));
        index=[warmup_low+1:warmup_high-1];
    end
    %save_keywords={'Save'};
    %fetch_keywords={'Fetch'};
    %allocator_keywords={'Add','Allocate'};
    %task_keywords={'Report';'Update_Structure';'GetRules';'UpdateMonitors'};
    data1=draw_profiler3(sprintf('%s/profile_EpochProcedure.csv',path),index);
    data2=draw_profiler3(sprintf('%s/profile_RuntimeTaskHandler.csv',path),index);
    data3=draw_profiler3(sprintf('%s/profile_SeparateMultiTaskMultiSwitchTaskHandler.csv',path),index);
    close all;
    out=categorize_profiler(data1,data2,data3,keywords);
end
