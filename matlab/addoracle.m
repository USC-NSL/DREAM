load('data/oracle/sduration');
foracle=f;
load('data/sduration');
f1=[f1;foracle(1,:)];f2=[f2;foracle(2,:)];f3=[f3;foracle(3,:)];f4=[f4;foracle(4,:)];
xvalues=[150 300 600 1200];
save('data/sduration');

clear
load('data/oracle/sarrival');
foracle=f;
load('data/sarrival');
f1=[f1;foracle(1,:)];f2=[f2;foracle(2,:)];f3=[f3;foracle(3,:)];f4=[f4;foracle(4,:)];
xvalues=[256 512 768 1024];
save('data/sarrival');

clear
load('data/oracle/sswitchpertask');
foracle=f;
load('data/sswitchpertask');
f1=[f1;foracle(1,:)];f2=[f2;foracle(2,:)];f3=[f3;foracle(3,:)];f4=[f4;foracle(4,:)];
xvalues=[2 4 8 16];
save('data/sswitchpertask');

clear
load('data/oracle/saccuracy');
foracle=f;
load('data/saccuracy');
f1=[f1;foracle(1,:)];f2=[f2;foracle(2,:)];f3=[f3;foracle(3,:)];f4=[f4;foracle(4,:)];
xvalues=[0.6,0.7,0.8,0.9];
save('data/saccuracy');

load('data/oracle/ssize');
foracle=f;
load('data/ssize');
f1=[f1;foracle(1,:)];f2=[f2;foracle(2,:)];f3=[f3;foracle(3,:)];f4=[f4;foracle(4,:)];
xvalues=[2^13, 2^14, 2^15, 2^16];
save('data/ssize');

load('data/oracle/sthreshold');
foracle=f;
load('data/sthreshold');
f1=[f1;foracle(1,:)];f2=[f2;foracle(2,:)];f3=[f3;foracle(3,:)];f4=[f4;foracle(4,:)];
xvalues=[0.5 1 2 4];
%draw_graphs(f1,f2,f3,f4,xvalues,'Threshold (MB)')
%screen2jpeg(figure(1), 'dec12/sthreshold/reject');screen2jpeg(figure(2), 'dec12/sthreshold/drop');screen2jpeg(figure(3), 'dec12/sthreshold/dropage');screen2jpeg(figure(4), 'dec12/sthreshold/satisfaction');
%close all
save('data/sthreshold');

