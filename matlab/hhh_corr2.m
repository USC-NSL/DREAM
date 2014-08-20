function out=hhh_corr2(path,ext,func1,func2)
     r=dir(path);
     filenames={};
     k=1;
    for i=1:length(r)
        if r(i).isdir
            if ~isempty(regexp(r(i).name,'SingleSwitch','once'))
                filenames{k}=r(i).name;
                k=k+1;
            end
        end    
    end
    %out=zeros(k-1,2);
    for i=1:k-1
        l=regexp(filenames{i}, '_', 'split'); 
        l=str2double(l{2}); 
        c=hhh_corr(sprintf('%s/%s',path,filenames{i}),ext,func1,func2);
        out(i,:)=[l,c];
    end
    out=sortrows(out,1);
end