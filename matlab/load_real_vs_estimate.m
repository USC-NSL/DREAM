function [satsdiff,abssatsdiff,diffs,absdiffs]=load_real_vs_estimate(path,accbound,filenames)
    drop_col=2;
    recall_col=4;
    precision_col=3;
    if (nargin<3)
        filenames={'dream_h5f';'equal';'fixed_32'};
        if (nargin<2)
            accbound=0.8;
        end
    end
    keywords={'MaxCoverHHDetection';'MultiSwitchChangeDetection';'MultiSwitch2'};
    accuracymetric_col=[recall_col,recall_col,precision_col];

    
    k=length(filenames)+1;
    try,
        x=csvread(sprintf('%s/equal/share.csv',path),1,0);
    catch e;
        x=csvread(sprintf('%s/dream_h5f/share.csv',path),1,0);
    end
    warmup_low=max(x(x(:,2)==1,1));
    last_task = max(x(:,2));
    warmup_high=min(x(x(:,2)==last_task,1));
    task_num=max(x(:,2));


    for i=1:length(filenames)
        display(sprintf('Loading %s',filenames{i}));
        task_num_2=task_num;
        satdiff=[];
        diff=[];
        js=[];
        for j=1:task_num
            task_type=getTaskType(sprintf('%s/%s/%d/config.xml',path,filenames{i},j),keywords);
            accuracy_col=accuracymetric_col(task_type);
            x=csvread(sprintf('%s/%s/%d/HHHMetrics.csv',path,filenames{i},j),1,0);
            if or(sum(x(:,1)<warmup_low)>0,sum(x(:,1)>warmup_high)>0)
                task_num_2=task_num_2-1;
                continue;
            end
            x(1,:)=[];
            droped_epochs=sum(x(:,drop_col)==1);
            lifetime=size(x,1)-droped_epochs;
            if (lifetime~=0)
                if (droped_epochs==0)
%                    sats=[sats; sum(x(x(:,drop_col)==0,accuracy_col)>=accbound)/lifetime];
                    js=[js;j];
                    y=csvread(sprintf('%s/%s/%d/acc.csv',path,filenames{i},j),1,0);
                    for t=1:size(y,1)
                        timeslot=y(t,1);
                        diff=[diff; x(x(:,1)==timeslot,accuracy_col)-y(t,2)];
                        if (y(t,2)>accbound)
                            satdiff=[satdiff;x(x(:,1)==timeslot,accuracy_col)-y(t,2)];
                        end
                    end
                end
            end
        end
        
        satsdiff(i,:)=[prctile(satdiff,5),prctile(satdiff,95),mean(satdiff)];
        abssatsdiff(i,:)=[prctile(abs(satdiff),5),prctile(abs(satdiff),95),mean(abs(satdiff))];
        diffs(i,:)=[prctile(diff,5),prctile(diff,95),mean(diff)];
        absdiffs(i,:)=[prctile(abs(diff),5),prctile(abs(diff),95),mean(abs(diff))];
    end    
end