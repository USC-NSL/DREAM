function [x,labels]=loadalgorithmmetrics(folder,subs)    
    for i=1:length(subs)
        sprintf('%s/%s',folder,subs{i})
        x(i,:,:)=csvread(sprintf('%s/%s',folder,subs{i}),1,0);        
    end
    labels=cell(3,1);
    l={};
    for j=1:size(x,2)
       l=[l;sprintf('%d',x(1,j,1)/1000)];
    end
    labels{2}=l;
    x(:,:,1)=[];
    m=importdata(sprintf('%s/%s',folder,subs{1}));
    labels{3}=m.colheaders(2:length(m.colheaders));
    
end