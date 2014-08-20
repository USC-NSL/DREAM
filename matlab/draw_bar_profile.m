function data=draw_bar_profile(path,xvalues,data)
    if nargin<3
        for i=1:length(xvalues)
            xi=xvalues(i);
            [savedelay,index]=categorize_profiler2(sprintf('%s/%d/dream_h5f',path,xi),{'Save'});
            fetchdelay=categorize_profiler2(sprintf('%s/%d/dream_h5f',path,xi),{'Fetch'},index);
            allocatordelay=categorize_profiler2(sprintf('%s/%d/dream_h5f',path,xi),{'Add','Allocate'},index);
            taskdelay=categorize_profiler2(sprintf('%s/%d/dream_h5f',path,xi),{'Report';'Update_Structure';'GetRules';'UpdateMonitors'},index);
            allocatordelay=sum(allocatordelay,1);
            taskdelay(3,:)=sum(taskdelay([3,4],:),1);
            taskdelay(4,:)=[];
            data(1,i,1)=mean(savedelay);
            data(2,i,1)=mean(fetchdelay);            
            data(4,i,1)=mean(allocatordelay);
            data(3,i,1)=mean(taskdelay(1,:));
            data(3,i,2)=mean(taskdelay(2,:));
            data(3,i,3)=mean(taskdelay(3,:));
        end
        data=data/1e6;
    end
    plotBarStackGroups(data,{'Save','Fetch','Task','Allocation'})
    ylabel('Delay (ms)');
    set(findall(gcf,'type','text'),'fontSize',14)
    set(findobj(gcf,'type','axes'),'fontsize',14)
    set(gcf,'OuterPosition',[500,500,375,360])
    l=legend('All / (Report & Estimate)','Configure counters','Runtime');
    set(l,'fontsize',9);
    xlim([0.5,4.5])
end