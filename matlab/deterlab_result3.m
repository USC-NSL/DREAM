path='E:/enl/measurement/DynamicMonitor/output/deterlabsim/256/dream_headroom0';
v=[];for i=1:128, try, x=csvread(sprintf('%s/%d/acc.csv',path,i)); v=[x;v];  catch e;end; end ;
dream_h0_256_drop=length(v)/128;
v=[];for i=1:128, try, x=csvread(sprintf('%s/%d/acc.csv',path,i)); v=[sum(x(:,2)>=0.8)/size(x,1);v];  catch e;end; end ;
dream_h0_256_sat=mean(v);

path='E:/enl/measurement/DynamicMonitor/output/deterlabsim/256/dream_headroom1';
v=[];for i=1:128, try, x=csvread(sprintf('%s/%d/acc.csv',path,i)); v=[x;v];  catch e;end; end ;
dream_h1_256_drop=length(v)/128;
v=[];for i=1:128, try, x=csvread(sprintf('%s/%d/acc.csv',path,i)); v=[sum(x(:,2)>=0.8)/size(x,1);v];  catch e;end; end ;
dream_h1_256_sat=mean(v);

path='E:/enl/measurement/DynamicMonitor/output/deterlabsim/256/dream_headroom5';
v=[];for i=1:128, try, x=csvread(sprintf('%s/%d/acc.csv',path,i)); v=[x;v];  catch e;end; end ;
dream_h5_256_drop=length(v)/128;
v=[];for i=1:128, try, x=csvread(sprintf('%s/%d/acc.csv',path,i)); v=[sum(x(:,2)>=0.8)/size(x,1);v];  catch e;end; end ;
dream_h5_256_sat=mean(v);

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

path='E:/enl/measurement/DynamicMonitor/output/deterlabsim/512/dream_headroom0';
v=[];for i=1:128, try, x=csvread(sprintf('%s/%d/acc.csv',path,i)); v=[x;v];  catch e;end; end ;
dream_h0_512_drop=length(v)/128;
v=[];for i=1:128, try, x=csvread(sprintf('%s/%d/acc.csv',path,i)); v=[sum(x(:,2)>=0.8)/size(x,1);v];  catch e;end; end ;
dream_h0_512_sat=mean(v);
path='E:/enl/measurement/DynamicMonitor/output/deterlabsim/512/dream_headroom1';
v=[];for i=1:128, try, x=csvread(sprintf('%s/%d/acc.csv',path,i)); v=[x;v];  catch e;end; end ;
dream_h1_512_drop=length(v)/128;
v=[];for i=1:128, try, x=csvread(sprintf('%s/%d/acc.csv',path,i)); v=[sum(x(:,2)>=0.8)/size(x,1);v];  catch e;end; end ;
dream_h1_512_sat=mean(v);
path='E:/enl/measurement/DynamicMonitor/output/deterlabsim/512/dream_headroom5';
v=[];for i=1:128, try, x=csvread(sprintf('%s/%d/acc.csv',path,i)); v=[x;v];  catch e;end; end ;
dream_h5_512_drop=length(v)/128;
v=[];for i=1:128, try, x=csvread(sprintf('%s/%d/acc.csv',path,i)); v=[sum(x(:,2)>=0.8)/size(x,1);v];  catch e;end; end ;
dream_h5_512_sat=mean(v);
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

drop=1-[dream_h0_256_drop, dream_h1_256_drop,dream_h5_256_drop, equal_256_drop, fixed_256_drop; dream_h0_512_drop,dream_h1_512_drop,dream_h5_512_drop ,equal_512_drop, fixed_512_drop]/equal_256_drop;
sat=[dream_h0_256_sat,dream_h1_256_sat,dream_h5_256_sat, equal_256_sat, fixed_256_sat; dream_h0_512_sat,dream_h1_512_sat,dream_h5_512_sat ,equal_512_sat, fixed_512_sat];
figure;
bar([drop(1,:);sat(1,:)])
l=legend('Dream_h0','Dream_h1','Dream_h5','Equal','Fixed');
set(l,'interpreter','none');
set(gca,'XTickLabel',{'Drop','Satisfaction'});

figure;
bar([drop(2,:);sat(2,:)])
l=legend('Dream_h0','Dream_h1','Dream_h5','Equal','Fixed');
set(l,'interpreter','none');
set(gca,'XTickLabel',{'Drop','Satisfaction'});