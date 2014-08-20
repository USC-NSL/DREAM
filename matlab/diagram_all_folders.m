util_col=5;
hx=[-2,-1,0,0.8,1.1];
r=dir(path);
filenames={};
k=1;
for i=1:length(r)
    if r(i).isdir
        if isempty(regexp(r(i).name,'^\.+$','once'))            
            if isempty(regexp(r(i).name,'groundtruth','once'))
                filenames{k}=r(i).name;
                k=k+1;
            end
        end
    end    
end
h=zeros(k-1,length(hx)-1);
f=zeros(k-1,2);
figure; hold all;
for i=1:k-1
   display(sprintf('Loading %s',filenames{i}));
   [h(i,:),s]=get_hist(sprintf('%s/%s',path,filenames{i}),hx);
   f(i,1)=h(i,1);
   numDrop=sum(cellfun((@(x)sum(x(:,2)==-1)>0),s));
   f(i,2)=numDrop/length(s);
   f(i,3)=h(i,2);   
   f(i,4)=h(i,size(h,2))/sum(h(i,3:size(h,2)));
   
   for j=1:task_num, 
       x=csvread(sprintf('%s/%s/%d/HHHMetrics.csv',path,filenames{i},j),1,0);
       u{j}=[x(:,1),x(:,util_col)]; 
   end; 
   m=max(cellfun((@(x) max(x(:,1))),u))+1; 
   z=zeros(m,length(u));
   for j=1:length(u), 
       x=u{j};
       indexes=and(x(:,1)>100,x(:,1)<600);
%        if (sum(indexes==0)>0)
%            display('1');
%        end
       z(x(indexes,1)+1,j)=x(indexes,2); %now z is timeseries of utilization with tasks on columns
       %z(x(:,1)+1,j)=x(:,2); %now z is timeseries of utilization with tasks on columns
   end; 
   f(i,5)=sum(sum(z))/equal_util_sum;
   plot(1:size(z,1), sum(z,2),'DisplayName',filenames{i});
end
l=legend('show');
set(l,'interpreter','none');
figure;
bar(f')
set(gca,'XTickLabel',{'Reject';'DropNum';'Drop';'Happy when active';'Utilization'})
l=legend(filenames);
set(l,'interpreter','none');
figure;
bar(h')
set(gca,'XTickLabel',{'Drop';'0-80';'80-100'})
l=legend(filenames);
set(l,'interpreter','none');