function output=calculate_sketch_memory(l,w,d)
    output=zeros(length(w),1);
    fl = floor(log(d.*w)/log(2));
    output=output+2*2.^fl-2;
    i=l==0;
    l(i)=32-fl(i);
    output=output+d.*w.*l;
%     if (l>0)
%         output=output+d*w*l;
%     else
%         output=output+d*w*(32-fl);
%     end
end