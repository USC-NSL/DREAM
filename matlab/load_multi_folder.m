function stats=load_multi_folder(path,file_filter)
    r=dir(path);
    filenames={};
    k=1;
    for i=1:length(r)
        if r(i).isdir
            if isempty(regexp(r(i).name,'^\.+$','once'))
                if ~isempty(regexp(r(i).name,file_filter,'once'))
                    filenames{k}=r(i).name;
                    k=k+1;
                end                
            end
        end    
    end
    stats=cell(1,k-1);
    for i=1:k-1
        file =sprintf('%s/%s/HHHMetrics.csv',path,filenames{i});        
        stats{i}=csvread(file,1,0);
        f=regexp(regexprep(filenames{i},'[^_0-9]',''),'_','split');
        t=zeros(1,length(f));
        for j=1:length(f)
           t(j)=str2double(f{j});
        end
        stats{i}=[t,stats{i}];
    end
end