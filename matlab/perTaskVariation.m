function [allocationVar,accuracyVar]=perTaskVariation(path)
    x=csvread(path,1,0);
    task_num=max(x(:,2));
    %ignore warmup/cooldown
    firstEnd=max(x(x(:,2)==1,1));
    lastStart=min(x(x(:,2)==max(x(:,2)),1));
    ignoreTasks = unique([x(x(:,1)<=firstEnd,2);x(x(:,1)>=lastStart,2)]);
    taskss=unique(x(:,2));%1:task_num; some tasks may be rejected
    tasks=[];
    i=1;
    for j=1:length(taskss)
        if isempty(find(ignoreTasks==taskss(j), 1))
            tasks(i)=taskss(j);
            i=i+1;
        end
    end
    
    allocationVar=zeros(1,task_num);
    accuracyVar=zeros(1,task_num);
    for t=tasks
        talloc=x(x(:,2)==t,:);
        sws=unique(talloc(:,3));
        swAllocVar=zeros(length(sws),1);
        swAccVar=zeros(length(sws),1);
        for j=1:length(sws)
            sw=sws(j);
            swAllocVar(j)=var(talloc(talloc(:,3)==sw,4));
            swAccVar(j)=var(talloc(talloc(:,3)==sw,6));
        end
        allocationVar(t)=mean(swAllocVar);
        accuracyVar(t)=mean(swAccVar);
    end
end