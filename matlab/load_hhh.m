function [x,t]=load_hhh(file)
    fid = fopen(file);
    data=textscan(fid,'%d%s%f','Delimiter',',');
    fclose(fid);
    min_time=min(data{1});
    max_time=max(data{1});
    t=min_time:max_time;
    x=cell(max_time-min_time+1,1);
    for i=1:length(t)
        indeces=find(data{1}==t(i));
        if (isempty(indeces))
            x{i}=[];
        else
            x{i}(:,1)=data{3}(indeces);
            f=strfind(data{2}(indeces),'_');        
            x{i}(:,2)=cellfun('length',f);
        end
    end
end