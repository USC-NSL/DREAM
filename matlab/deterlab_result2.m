path='E:/enl/measurement/DynamicMonitor/output/deterlabsim/256/dream';
v=[];for i=1:128, try, x=csvread(sprintf('%s/%d/acc.csv',path,i)); v=[x;v];  catch e;end; end ;
dream_256_drop=length(v)/128;
v=[];for i=1:128, try, x=csvread(sprintf('%s/%d/acc.csv',path,i)); v=[sum(x(:,2)>=0.8)/size(x,1);v];  catch e;end; end ;
dream_256_sat=mean(v);

path='E:/enl/measurement/DynamicMonitor/output/deterlabsim/256/dream_headroom0';
v=[];for i=1:128, try, x=csvread(sprintf('%s/%d/acc.csv',path,i)); v=[x;v];  catch e;end; end ;
dream_h0_256_drop=length(v)/128;
v=[];for i=1:128, try, x=csvread(sprintf('%s/%d/acc.csv',path,i)); v=[sum(x(:,2)>=0.8)/size(x,1);v];  catch e;end; end ;
dream_h0_256_sat=mean(v);

path='E:/enl/measurement/DynamicMonitor/output/deterlabsim/256/equal';
v=[];for i=1:128, try, x=csvread(sprintf('%s/%d/acc.csv',path,i)); v=[x;v];  catch e;end; end ;
equal_256_drop=length(v)/128;
v=[];for i=1:128, try, x=csvread(sprintf('%s/%d/acc.csv',path,i)); v=[sum(x(:,2)>=0.8)/size(x,1);v];  catch e;end; end ;
equal_256_sat=mean(v);
path='E:/enl/measurement/DynamicMonitor/output/deterlabsim/256/fixed';
v=[];for i=1:128, try, x=csvread(sprintf('%s/%d/acc.csv',path,i)); v=[x;v];  catch e;end; end ;
fixed_256_drop=length(v)/128;
v=[];for i=1:128, try, x=csvread(sprintf('%s/%d/acc.csv',path,i)); v=[sum(x(:,2)>=0.8)/size(x,1);v];  catch e;end; end ;
fixed_256_sat=mean(v);

path='E:/enl/measurement/DynamicMonitor/output/deterlabsim/512/dream';
v=[];for i=1:128, try, x=csvread(sprintf('%s/%d/acc.csv',path,i)); v=[x;v];  catch e;end; end ;
dream_512_drop=length(v)/128;
v=[];for i=1:128, try, x=csvread(sprintf('%s/%d/acc.csv',path,i)); v=[sum(x(:,2)>=0.8)/size(x,1);v];  catch e;end; end ;
dream_512_sat=mean(v);
path='E:/enl/measurement/DynamicMonitor/output/deterlabsim/512/equal';
v=[];for i=1:128, try, x=csvread(sprintf('%s/%d/acc.csv',path,i)); v=[x;v];  catch e;end; end ;
equal_512_drop=length(v)/128;
v=[];for i=1:128, try, x=csvread(sprintf('%s/%d/acc.csv',path,i)); v=[sum(x(:,2)>=0.8)/size(x,1);v];  catch e;end; end ;
equal_512_sat=mean(v);
path='E:/enl/measurement/DynamicMonitor/output/deterlabsim/256/fixed';
v=[];for i=1:128, try, x=csvread(sprintf('%s/%d/acc.csv',path,i)); v=[x;v];  catch e;end; end ;
fixed_512_drop=length(v)/128;
v=[];for i=1:128, try, x=csvread(sprintf('%s/%d/acc.csv',path,i)); v=[sum(x(:,2)>=0.8)/size(x,1);v];  catch e;end; end ;
fixed_512_sat=mean(v);

drop=1-[dream_256_drop, dream_h0_256_drop, equal_256_drop, fixed_256_drop; dream_512_drop,0 ,equal_512_drop, fixed_512_drop]/equal_256_drop;
sat=[dream_256_sat,dream_h0_256_sat, equal_256_sat, fixed_256_sat; dream_512_sat,0 ,equal_512_sat, fixed_512_sat];
figure;
bar([drop(1,:);sat(1,:)])
legend('Dream','Equal','Fixed');
set(gca,'XTickLabel',{'Drop','Satisfaction'});

figure;
bar([drop(2,:);sat(2,:)])
legend('Dream','Equal','Fixed');
set(gca,'XTickLabel',{'Drop','Satisfaction'});