util_col=5;
drop_col=2;
precision_col=4;
accbound=0.8;

r=dir(path);
filenames={};
k=1;
for i=1:length(r)
    if r(i).isdir
        if isempty(regexp(r(i).name,'^\.+$','once'))            
            if isempty(regexp(r(i).name,'groundtruth','once'))
                filenames{k}=r(i).name;
                k=k+1;
            end
        end
    end    
end

figure;
hold all;

rejected_task=zeros(k-1,1);
droped_task=zeros(k-1,1);
drop_age=zeros(k-1,1);
satisfaction=zeros(k-1,1);
utilization=zeros(k-1,1);

x=csvread(sprintf('%s/equal/share.csv',path),1,0);
warmup_low=max(x(x(:,2)==1,1));
last_task = max(x(:,2));
warmup_high=min(x(x(:,2)==last_task,1));
task_num=max(x(:,2));


for i=1:k-1
    display(sprintf('Loading %s',filenames{i}));
    u=cell(task_num,1);
    task_num_2=task_num;
    sats=[];
    js=[];
    for j=1:task_num
        x=csvread(sprintf('%s/%s/%d/HHHMetrics.csv',path,filenames{i},j),1,0);
        u{j}=[x(:,1),x(:,util_col)];%utility has separate definition of warmup
        if or(sum(x(:,1)<warmup_low)>0,sum(x(:,1)>warmup_high)>0)
            task_num_2=task_num_2-1;
            continue;
        end
        x(1,:)=[];
        droped_epochs=sum(x(:,drop_col)==1);
        lifetime=size(x,1)-droped_epochs;
        if (lifetime==0)
            rejected_task(i)=rejected_task(i)+1;
        else
            if (droped_epochs>0)
                droped_task(i)=droped_task(i)+1;
                drop_age(i)=drop_age(i)+lifetime/size(x,1);
            end
            if (droped_epochs==0)
                sats=[sats; sum(x(x(:,drop_col)==0,precision_col)>=accbound)/lifetime];
                js=[js;j];
            end
        end
        
    end
    if (droped_task(i)>0)
         drop_age(i)=drop_age(i)/droped_task(i);   
    end    
    %if (rejected_task(i)<task_num_2)
     satisfaction(i)=prctile(sats,5);   
    %end
    rejected_task(i)=rejected_task(i)/task_num_2;
    droped_task(i)=droped_task(i)/task_num_2;
    u(cellfun('length',u)==0)=[];
    m=max(cellfun((@(x) max(x(:,1))),u))+1; 
    z=zeros(m,length(u));
    for j=1:length(u), 
       x=u{j};
       indexes=and(x(:,1)>warmup_low,x(:,1)<warmup_high);
       z(x(indexes,1)+1,j)=x(indexes,2); %now z is timeseries of utilization with tasks on columns
       %z(x(:,1)+1,j)=x(:,2); %now z is timeseries of utilization with tasks on columns
    end; 
    %utilization(i)=sum(sum(z))/equal_util_sum;
    plot(1:size(z,1), sum(z,2),'DisplayName',filenames{i});
end
l=legend('show');
set(l,'interpreter','none');

f=[rejected_task,droped_task,drop_age,satisfaction];
figure;
bar(f');
set(gca,'XTickLabel',{'Reject';'DropNum';'Drop age';'Happy when active';'Utilization'})
l=legend(filenames);
set(l,'interpreter','none');