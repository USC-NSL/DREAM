function [sat,rej,drop]=load_real2(path,xvalues,path2,xvalues2,ordering)
    if (nargin==2)
        for i=1:length(xvalues)
            xvaluesi=xvalues(i);
            [sat(i,:,:),rej(i,:),drop(i,:)]=load_real(sprintf('%s/%d',path,xvaluesi));
        end
    else
        for i=1:length(xvalues)
            xvaluesi=xvalues(i);
            ii=ordering(i);
            [sat(ii,:,:),rej(ii,:),drop(ii,:)]=load_real(sprintf('%s/%d',path,xvaluesi));
        end
        for i=1:length(xvalues2)
            xvaluesi=xvalues2(i);
            ii=ordering(i+length(xvalues));
            [sat(ii,:,:),rej(ii,:),drop(ii,:)]=load_real(sprintf('%s/%d',path2,xvaluesi));
        end
    end
end