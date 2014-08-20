function data=draw_bar_profile2(path,xvalues,data)
    if nargin<3
        for i=1:length(xvalues)
            xi=xvalues(i);
            [allocatordelay,index]=categorize_profiler3(sprintf('%s/%d/dream_h5f',path,xi),{'Add','Allocate'});
            %savedelay=categorize_profiler2(sprintf('%s/%d/dream_h5f',path,xi),{'Save'},index);
            %fetchdelay=categorize_profiler2(sprintf('%s/%d/dream_h5f',path,xi),{'Fetch'},index);            
            taskdelay=categorize_profiler3(sprintf('%s/%d/dream_h5f',path,xi),{'Report';'Update_Structure';'GetRules';'UpdateMonitors'},index);
            allocatordelay=sum(allocatordelay,1);
            
%            taskdelay(3,:)=sum(taskdelay([3,4],:),1);
%            taskdelay(4,:)=[];
%            data(1,i)=mean(savedelay);
%            data(2,i)=mean(fetchdelay);            
            %data(3,i)=mean(allocatordelay);
            %data(4,i)=mean(taskdelay(1,:));
            %data(5,i)=mean(taskdelay(2,:));
%            data(6,i)=mean(taskdelay(3,:));
            
            data(1,i)=mean(allocatordelay);
            data(2,i)=prctile(allocatordelay,5);
            data(3,i)=prctile(allocatordelay,95);
            
%             data(1,i)=prctile(savedelay,5);
%             data(2,i)=prctile(fetchdelay,5);            
%             data(3,i)=prctile(allocatordelay,5);
%             data(4,i)=prctile(taskdelay(1,:),5);
%             data(5,i)=prctile(taskdelay(2,:),5);
%             data(6,i)=prctile(taskdelay(3,:),5);
        end
        data=data/1e6;
    end
    bar(data','stacked');
    ylabel('Delay (ms)');
    xlabel('Switch capacity');
    set(gca,'XTickLabel', arrayfun(@num2str, xvalues, 'UniformOutput', false));
    set(findall(gcf,'type','text'),'fontSize',13)
    set(findobj(gcf,'type','axes'),'fontsize',13)
    set(gcf,'OuterPosition',[500,500,375,360])
    l=legend('Save','Fetch','Allocation','Report & Estimate','Configure counters','Runtime');
    set(l,'fontsize',10);
    xlim([0.5,4.5])
    %c=gray(6);
   % c=c(randperm(length(c)),:);
    %colormap(c);
end