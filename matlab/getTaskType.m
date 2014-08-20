function k=getTaskType(filename,keywords)
    filetext = fileread(filename);
    for k=1:length(keywords)
        index = regexp(filetext,keywords{k},'match');
        if ~isempty(index)
            return;
        end
    end
    k=0;
end