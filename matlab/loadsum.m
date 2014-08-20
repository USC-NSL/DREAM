function [data]=loadsum(path)
r=dir(path);
filenames={};
k=1;
for i=1:length(r)
    if ~r(i).isdir
        filenames{k}=r(i).name;
        k=k+1;
    end    
end

for i=1:k-1
    x=csvread(sprintf('%s/%s',path,filenames{i}));
    data{i}=x; 
end
end