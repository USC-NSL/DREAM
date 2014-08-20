r=dir(path);
filenames={};
k=1;
for i=1:length(r)
    if isempty(regexp(r(i).name,'^\.+$','once'))            
        filenames{k}=r(i).name;
        k=k+1;
    end
end

m=zeros(length(filenames),4);
s=zeros(length(filenames),4);
xvalues=zeros(length(filenames),1);
for i=1:length(filenames)
    data=importdata(sprintf('%s/%s',path,filenames{i}));
    xvalues(i)=str2double(regexp(filenames{i},'\d+','match'));
    x=data.data;
    if (size(x,2)==2)
        x=x(:,2);
    end
    epochs=100;
    x=x(1:epochs*4);
    x=reshape(x,4,epochs)';
    x([1:5,96:100],:)=[];
    m(i,:)=mean(x);
    s(i,:)=std(x);
end

[xvalues,index]=sort(xvalues);
m=m(index,:);
s=s(index,:);

figure;
bar(m);

figure;
bar(s);