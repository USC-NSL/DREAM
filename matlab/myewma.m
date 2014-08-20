function out=myewma(x,l,init)
    out(1,:)=init;
    for i=1:size(x,1)
        out(i+1,:)=out(i,:)*l+x(i,:)*(1-l);
    end
    out(1,:)=[];
end