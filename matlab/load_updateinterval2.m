clear;
path='F:/enl/measurement/tupdateinterval_a512';
files={'dream_h0f';'dream_h1f';'dream_h5f';'dream_h10f'};

xvalues=[2,4,8,16];

for i=1:4    
    [sat(i,:,:),rej(i,:),drop(i,:)]=load_real(sprintf('%s/%d',path,xvalues(i)),0.8,files);
end