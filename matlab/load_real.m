function [sat,rej,drop]=load_real(path,accbound,filenames)
    util_col=5;
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
    and_cols=accuracymetric_col;
    %keywords={'MultiSwitchHHHierarchicalSketch';'MultiSwitchSketch2';'SSCountMinMultiSwitch'};
    %accuracymetric_col=[precision_col,precision_col,precision_col];
    %and_cols=[recall_col,recall_col,recall_col];

    
    k=length(filenames)+1;
    rej=zeros(k-1,1);
    drop=zeros(k-1,1);
    drop_age=zeros(k-1,1);
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
        sats=[];
        js=[];
        for j=1:task_num
            task_type=getTaskType(sprintf('%s/%s/%d/config.xml',path,filenames{i},j),keywords);
            %if (task_type~=3)
            %       continue;
            %end
            accuracy_col=accuracymetric_col(task_type);
            and_col=and_cols(task_type);
            x=csvread(sprintf('%s/%s/%d/HHHMetrics.csv',path,filenames{i},j),1,0);
            if or(sum(x(:,1)<warmup_low)>0,sum(x(:,1)>warmup_high)>0)
                task_num_2=task_num_2-1;
                continue;
            end
            x(1,:)=[];
            droped_epochs=sum(x(:,drop_col)==1);
            lifetime=size(x,1)-droped_epochs;
            if (lifetime==0)
                rej(i)=rej(i)+1;
            else
                if (droped_epochs>0)
                    drop(i)=drop(i)+1;
                    drop_age(i)=drop_age(i)+lifetime/size(x,1);
                end
                if (droped_epochs==0)
                    sats=[sats; sum(x(and(x(:,drop_col)==0,x(:,and_col)>0),accuracy_col)>=accbound)/lifetime];
                    js=[js;j];
                end
            end

        end
        if (drop(i)>0)
             drop_age(i)=drop_age(i)/drop(i);   
        end    
        sat(i,:)=[prctile(sats,05),prctile(sats,95),mean(sats)];
        rej(i)=rej(i)/task_num_2;
        drop(i)=drop(i)/task_num_2;
    end    
end