function data=draw_profiler3(path,index)
    data=importdata(path);
    t=str2double(data.textdata(:,1));
    index=and(t>=min(index),t<=max(index));
    data.data=data.data(index);
    data.textdata=data.textdata(index,:);
%     maxX=0; 
%     figure; 
%     hold all
%     tags=unique(data.textdata(:,2));
%     for i=1:length(tags); 
%         [hx,h]=draw_profiler_hist(data,tags{i});
%         maxX=max([maxX,hx]); 
%         plot(hx/1000000,h,'DisplayName',tags{i},'LineWidth',2); 
%     end; 
%     l=legend('show'); 
%     set(l,'interpreter','none');
%     axis([0,maxX/1000000,0,1]);
%     xlabel('Duration (ms)');
%     ylabel('CDF');
end