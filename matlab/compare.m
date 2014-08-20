t='Direction A'; 
alg='SingleSwitch'; 
parent='0.010/max_';
folders={'1_512';'1_4096';'1_32768'};
folders2={};
for i=1:length(folders)
    folders2{i}=sprintf('%s%s',parent,folders{i});
end
for i =1:length(cols),
    drawalgorithmmetric(alg,folders2,folders,t,cols(i));  
    print(gcf,'-djpeg',sprintf('f/%s_%s_%d.jpg',alg,t,cols(i))); 
    close; 
end