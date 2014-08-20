%tasks is a 3d matrice, each 2d columns are allocation, estimated accuracy,
%precision, recall
function [tasks]=load_multitask(path)
    col=[9,10];
    fid=fopen(sprintf('%s/share.csv',path),'r');
    input_text=textscan(fid,'%s',1,'delimiter','\n');
    input_text=input_text{1};
    fclose(fid);
    filters=regexp(input_text,'\([10_]+\)','match');
    filters=filters{1};
    for i=1:length(filters)
        filters{i}=strrep(filters{i},'(','');
        filters{i}=strrep(filters{i},')','');
        filters{i}=strrep(filters{i},'_','=');
    end
    filters=sort(filters);
    x=csvread(sprintf('%s/share.csv',path),1,0);
    tasks_num=length(filters);
    
    % find the first folder
    r=dir(path);
    for i=1:length(r)
        if r(i).isdir
            if isempty(regexp(r(i).name,'^\.+$','once'))
                folder_name=r(i).name;
                break;
            end
        end    
    end
    path=sprintf('%s/%s',path,folder_name);
    for i=1:tasks_num
        %load algorithm allocation hhh
        %find the folder
        r=dir(path);
        found=0;
        for j=1:length(r)
            if r(j).isdir
                folder_name=r(j).name;
                if ~isempty(strfind(folder_name,filters{i}))
                    found=1;
                    break;
                end    
            end
        end
        if (~found)
            display(sprintf('folder not found for filter %s',folder_name));
            return;
        end
        task_folder_path=sprintf('%s/%s',path,folder_name);
        task_hhh_data=csvread(sprintf('%s/HHHMetrics.csv',task_folder_path),1,0);
        tasks(:,[1 2],i)=[x(:,2*i+[0,1])];        
        tasks(1:size(task_hhh_data,1),2+[1:length(col)],i)=task_hhh_data(:,col);
    end
end